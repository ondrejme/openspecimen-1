/*
 * Created on Jan 17, 2007
 * @author
 */

package edu.wustl.catissuecore.action.annotations;

import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import edu.common.dynamicextensions.domain.integration.EntityMap;
import edu.common.dynamicextensions.exception.DynamicExtensionsSystemException;
import edu.common.dynamicextensions.ui.webui.util.WebUIManager;
import edu.common.dynamicextensions.ui.webui.util.WebUIManagerConstants;
import edu.wustl.catissuecore.actionForm.AnnotationDataEntryForm;
import edu.wustl.catissuecore.bizlogic.AnnotationBizLogic;
import edu.wustl.catissuecore.util.global.AppUtility;
import edu.wustl.catissuecore.util.global.Constants;
import edu.wustl.common.action.BaseAction;
import edu.wustl.common.beans.SessionDataBean;
import edu.wustl.common.exception.BizLogicException;
import edu.wustl.common.util.logger.Logger;

/**
 * @author preeti_munot To change the template for this generated type comment
 *         go to Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and
 *         Comments
 */
public class LoadDynamicExtentionsDataEntryPageAction extends BaseAction
{

	/**
	 * logger.
	 */
	private transient final Logger logger = Logger
			.getCommonLogger( LoadDynamicExtentionsDataEntryPageAction.class );

	/**
	 * @param mapping - mapping
	 * @param form - ActionForm
	 * @param request - HttpServletRequest object
	 * @param response - HttpServletResponse
	 * @return ActionForward
	 * @throws Exception - Exception
	 */
	/**
	 * @param mapping
	 *            object of ActionMapping
	 * @param form
	 *            object of ActionForm
	 * @param request
	 *            object of HttpServletRequest
	 * @param response
	 *            object of HttpServletResponse
	 * @throws Exception
	 *             generic exception
	 * @return ActionForward : ActionForward
	 */
	@Override
	protected ActionForward executeAction(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws Exception
	{
		final AnnotationDataEntryForm annotationDataEntryForm = (AnnotationDataEntryForm) form;
		this.updateCache( request, annotationDataEntryForm );

		// Set as request attribute
		final String dynExtDataEntryURL = this.getDynamicExtensionsDataEntryURL( request,
				annotationDataEntryForm );
		request.setAttribute( AnnotationConstants.DYNAMIC_EXTN_DATA_ENTRY_URL_ATTRIB,
				dynExtDataEntryURL );
		request.setAttribute( Constants.OPERATION, "DefineDynExtDataForAnnotations" );
		return mapping.findForward( Constants.SUCCESS );
	}

	/**
	 * @param request : request
	 * @param annotationDataEntryForm : annotationDataEntryForm
	 * @throws DynamicExtensionsSystemException : DynamicExtensionsSystemException
	 * @throws BizLogicException : BizLogicException
	 */
	private void updateCache(HttpServletRequest request,
			AnnotationDataEntryForm annotationDataEntryForm)
			throws DynamicExtensionsSystemException, BizLogicException
	{
		final String staticEntityId = annotationDataEntryForm.getParentEntityId();
		final String dynEntContainerId = annotationDataEntryForm.getSelectedAnnotation();

		// Set into Cache
		// CatissueCoreCacheManager cacheManager =
		// CatissueCoreCacheManager.getInstance();
		final HttpSession session = request.getSession();
		final Long entityMapId = this.getEntityMapId( AppUtility.toLong( staticEntityId ),
				AppUtility.toLong( dynEntContainerId ) );
		session.setAttribute( AnnotationConstants.SELECTED_ENTITY_MAP_ID, entityMapId );
		session.setAttribute( AnnotationConstants.SELECTED_STATIC_ENTITYID, annotationDataEntryForm
				.getSelectedStaticEntityId() );
		session.setAttribute( AnnotationConstants.SELECTED_STATIC_ENTITY_RECORDID,
				annotationDataEntryForm.getSelectedStaticEntityRecordId() );
	}

	/**
	 * @param staticEntityId : staticEntityId
	 * @param dynamicEntityContainerId : dynamicEntityContainerId)
	 * @return Long : Long
	 * @throws DynamicExtensionsSystemException : DynamicExtensionsSystemException
	 * @throws BizLogicException : BizLogicException
	 */
	private Long getEntityMapId(Long staticEntityId, Long dynamicEntityContainerId)
			throws DynamicExtensionsSystemException, BizLogicException
	{
		if (staticEntityId != null)
		{
			final AnnotationBizLogic annotationBizLogic = new AnnotationBizLogic();
			final List < EntityMap > entityMapsForStaticEntity = annotationBizLogic
					.getListOfDynamicEntities( staticEntityId );
			final EntityMap entityMap = this.getEntityMapForSelectedDE( dynamicEntityContainerId,
					entityMapsForStaticEntity );
			if (entityMap != null)
			{
				return entityMap.getId();
			}
		}
		return null;
	}

	/**
	 * @param deContainerId : deContainerId
	 * @param entityMapsForStaticEntity : entityMapsForStaticEntity
	 * @return EntityMap : EntityMap
	 */
	private EntityMap getEntityMapForSelectedDE(Long deContainerId,
			List < EntityMap > entityMapsForStaticEntity)
	{
		if (( deContainerId != null ) && ( entityMapsForStaticEntity != null ))
		{
			EntityMap entityMap = null;
			final Iterator < EntityMap > entityMapIter = entityMapsForStaticEntity.iterator();
			while (entityMapIter.hasNext())
			{
				entityMap = entityMapIter.next();
				if (( entityMap != null ) && ( entityMap.getContainerId() != null ))
				{
					if (entityMap.getContainerId().longValue() == deContainerId.longValue())
					// If matches the specified DE container id
					{
						return entityMap;
					}
				}
			}
		}
		return null;
	}

	/**
	 * @param request : request
	 * @param annotationDataEntryForm : annotationDataEntryForm
	 * @return String : String
	 */
	private String getDynamicExtensionsDataEntryURL(HttpServletRequest request,
			AnnotationDataEntryForm annotationDataEntryForm)
	{
		String dynExtDataEntryURL = request.getContextPath()
				+ WebUIManager.getLoadDataEntryFormActionURL();
		final SessionDataBean sessionbean = (SessionDataBean) request.getSession().getAttribute(
				edu.wustl.catissuecore.util.global.Constants.SESSION_DATA );
		final String userId = sessionbean.getUserId().toString();
		String isAuthenticatedUser = "false";
		if (userId != null)
		{
			isAuthenticatedUser = "true";
		}

		// Append container id
		this.logger.info( "Load data entry page for Dynamic Extension Entity ["
				+ annotationDataEntryForm.getSelectedAnnotation() + "]" );
		dynExtDataEntryURL = dynExtDataEntryURL + "&"
				+ WebUIManagerConstants.CONATINER_IDENTIFIER_PARAMETER_NAME + "="
				+ annotationDataEntryForm.getSelectedAnnotation();
		if (request.getParameter( "recordId" ) != null)
		{
			this.logger.info( "Loading details of record id [" + request.getParameter( "recordId" )
					+ "]" );
			dynExtDataEntryURL = dynExtDataEntryURL + "&"
					+ WebUIManagerConstants.RECORD_IDENTIFIER_PARAMETER_NAME + "="
					+ request.getParameter( "recordId" );
		}
		final String operation = request.getParameter( "operation" );
		final String selectedAnnotation = request.getParameter( "selectedAnnotation" );
		final String callbackURL = AnnotationConstants.CALLBACK_URL_PATH_ANNOTATION_DATA_ENTRY
				+ "?" + "editOperation=" + operation + "@selectedAnnotation=" + selectedAnnotation;
		// append callback url
		dynExtDataEntryURL = dynExtDataEntryURL + "&" + WebUIManager.getCallbackURLParamName()
				+ "=" + request.getContextPath() + callbackURL + "&isAuthenticatedUser="
				+ isAuthenticatedUser;
		return dynExtDataEntryURL;
	}

}
