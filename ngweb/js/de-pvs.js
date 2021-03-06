var openspecimen = openspecimen || {}
openspecimen.ui = openspecimen.ui || {};
openspecimen.ui.fancy = openspecimen.ui.fancy || {};

openspecimen.ui.fancy.Pvs = function() {
  var baseUrl = '../../rest/ng/permissible-values/';

  var pvsCacheMap = {};
 
  this.getPvs = function(queryTerm, callback, field) {
    var filters = $.extend({}, field.settings || {});
    if (queryTerm) {
      filters.searchString = queryTerm;
    }

    var xhr = $.ajax({type: 'GET', url: baseUrl, data: filters});
    xhr.done(
      function(pvs) {
        var result = [];
        for (var i = 0; i < pvs.length; ++i) {
          result.push({id: pvs[i].id, text: pvs[i].value});
        }

        callback(result);
      }
    ).fail(
      function(data) {
        alert("Failed to load PVs list");
      }
    );
  };

  this.getPv = function(pvId, callback) {
    var pv = pvsCacheMap[pvId];
    if (pv) {
      callback(pv);
      return;
    }

    $.ajax({type: 'GET', url: baseUrl + pvId})
      .done(function(data) {
        var result = {id: data.id, text: data.value};
        pvsCacheMap[pvId] = result;
        callback(result);
      })
      .fail(function(data) {
        alert("Failed to retrieve site")
      });
  };
};

var pvSvc = new openspecimen.ui.fancy.Pvs();

openspecimen.ui.fancy.PvField = function(params) {
  this.inputEl = null;

  this.control = null;

  this.value = '';

  this.validator;

  var field = params.field;
  var id = params.id;
  var timeout = undefined;
  var that = this;

  var qFunc = function(qTerm, qCallback) {
    var timeInterval = 500;
    if (qTerm.length == 0) {
      timeInterval = 0;
    }

    if (timeout != undefined) {
      clearTimeout(timeout);
    }

    timeout = setTimeout(
      function() { 
        pvSvc.getPvs(qTerm, qCallback, field); 
      }, 
      timeInterval);
  };

  var onChange = function(selected) { 
    if (selected) {
      this.value = selected.id;
    } else {
      this.value = '';
    }
  };

  var initSelectedPv = function(pvId, elem, callback) {
    if (!pvId) {
      return;
    }
    pvSvc.getPv(pvId, callback);
  };

  this.render = function() {
    this.inputEl = $("<input/>")
      .prop({id: id, title: field.toolTip, value: field.defaultValue})
      .css("border", "0px").css("padding", "0px")
      .val("")
      .addClass("form-control");
    this.validator = new edu.common.de.FieldValidator(field.validationRules, this);
    return this.inputEl;
  };

  this.postRender = function() {
    this.control = new Select2Search(this.inputEl);
    this.control.onQuery(qFunc).onChange(onChange);
    this.control.setValue(this.value);

    this.control.onInitSelection(
      function(elem, callback) {
        initSelectedPv(that.value, elem, callback);
      }
    ).render();

  };

  this.getName = function() {
    return field.name;
  };

  this.getCaption = function() {
    return field.caption;
  };

  this.getTooltip = function() {
    return field.toolTip ? field.toolTip : field.caption;
  };

  this.getValue = function() {
    var val = this.control.getValue();
    if (val) {
      val = val.id;
    }

    return {name: field.name, value: val ? val : ''};
  };

  this.getDisplayValue = function() {
    if(!this.control) {
      this.postRender();
    }
    var val = this.control.getValue();
    if (val) {
      var displayValue = val.text;
    }
    return {name: field.name, value: displayValue ? displayValue : '' };
  }

  this.setValue = function(recId, value) {
    this.recId = recId;
    this.value = value ? value : '';
    if (this.control) {
      this.control.setValue(value);
    }
  };

  this.validate = function() {
    return this.validator.validate();
  };

  this.getPrintEl = function() {
    return edu.common.de.Utility.getPrintEl(this);
  };
};

edu.common.de.FieldManager.getInstance()
  .register({
    name: "pvField", 
    displayName: "PV Dropdown",
    fieldCtor: openspecimen.ui.fancy.PvField
  }); 
