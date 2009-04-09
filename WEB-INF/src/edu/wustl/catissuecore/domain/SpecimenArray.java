
package edu.wustl.catissuecore.domain;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import edu.wustl.catissuecore.actionForm.SpecimenArrayAliquotForm;
import edu.wustl.catissuecore.actionForm.SpecimenArrayForm;
import edu.wustl.catissuecore.bizlogic.BizLogicFactory;
import edu.wustl.catissuecore.bizlogic.SpecimenArrayBizLogic;
import edu.wustl.catissuecore.util.SearchUtil;
import edu.wustl.catissuecore.util.global.Constants;
import edu.wustl.common.actionForm.AbstractActionForm;
import edu.wustl.common.actionForm.IValueObject;
import edu.wustl.common.exception.AssignDataException;
import edu.wustl.common.exception.BizLogicException;
import edu.wustl.dao.exception.DAOException;

/**
 * @author gautam_shetty
 * @author Ashwin Gupta
 * @hibernate.joined-subclass table="CATISSUE_SPECIMEN_ARRAY"
 * @hibernate.joined-subclass-key column="IDENTIFIER"
 */
public class SpecimenArray extends Container
{

	/**
	 * Serial Version ID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * specimenArrayType.
	 */
	protected SpecimenArrayType specimenArrayType;

	/**
	 * createdBy User.
	 */
	protected User createdBy;

	//protected StorageContainer storageContainer;

	/**
	 * HashSet containing specimenArrayContentCollection.
	 */
	protected Collection specimenArrayContentCollection = new HashSet();

	/**
	 * available.
	 */
	protected Boolean available;

	/**
	 * aliquot.
	 */
	private transient boolean aliquot = false;

	/**
	 * aliquotCount.
	 */
	private transient int aliquotCount;

	/**
	 * HashMap containing aliqoutMap.
	 */
	private transient Map aliqoutMap = new HashMap();

	//Ordering System ---- vaishali
	/**
	 * HashSet containing newSpecimenArrayOrderItemCollection.
	 */
	protected Collection newSpecimenArrayOrderItemCollection = new HashSet();

	/**
	 * @return the newSpecimenArrayOrderItemCollection
	 * @hibernate.collection-one-to-many class="edu.wustl.catissuecore.domain.
	 * NewSpecimenArrayOrderItem" cascade="save-update" lazy="false"
	 * @hibernate.set name="newSpecimenArrayOrderItemCollection" table="CATISSUE_NEW_SP_AR_ORDER_ITEM"
	 * @hibernate.collection-key column="SPECIMEN_ARRAY_ID"
	 */
	public Collection getNewSpecimenArrayOrderItemCollection()
	{
		return newSpecimenArrayOrderItemCollection;
	}

	/**
	 * @param newSpecimenArrayOrderItemCollection the newSpecimenArrayOrderItemCollection to set
	 */
	public void setNewSpecimenArrayOrderItemCollection(
			Collection newSpecimenArrayOrderItemCollection)
	{
		this.newSpecimenArrayOrderItemCollection = newSpecimenArrayOrderItemCollection;
	}

	//Ordering System End

	/**
	 * Default Constructor.
	 */
	public SpecimenArray()
	{
		super();
	}

	/**
	 * Constructor with action form.
	 * @param actionForm abstract action form.
	 * @throws AssignDataException AssignDataException.
	 */
	public SpecimenArray(AbstractActionForm actionForm) throws AssignDataException
	{
		super();
		setAllValues(actionForm);
	}

	/**
	 * @return Returns the createdBy.
	 * @hibernate.many-to-one column="CREATED_BY_ID" class="edu.wustl.catissuecore.domain.User"
	 * constrained="true"
	 */
	public User getCreatedBy()
	{
		return createdBy;
	}

	/**
	 * @param createdBy The createdBy to set.
	 */
	public void setCreatedBy(User createdBy)
	{
		this.createdBy = createdBy;
	}

	/**
	 * @return Returns the specimenArrayContentCollection.
	 * @hibernate.set name="specimenArrayContentCollection" table="CATISSUE_SPECI_ARRAY_CONTENT"
	 * cascade="none" inverse="false" lazy="false"
	 * @hibernate.collection-key column="SPECIMEN_ARRAY_ID"
	 * @hibernate.collection-one-to-many class="edu.wustl.catissuecore.domain.SpecimenArrayContent"
	 */
	public Collection getSpecimenArrayContentCollection()
	{
		return specimenArrayContentCollection;
	}

	/**
	 * @param specimenArrayContentCollection The specimenArrayContentCollection to set.
	 */
	public void setSpecimenArrayContentCollection(Collection specimenArrayContentCollection)
	{
		this.specimenArrayContentCollection = specimenArrayContentCollection;
	}

	/**
	 * @return Returns the specimenArrayType.
	 * @hibernate.many-to-one column="SPECIMEN_ARRAY_TYPE_ID" class="edu.wustl.
	 * catissuecore.domain.SpecimenArrayType"
	 * constrained="true"
	 */
	public SpecimenArrayType getSpecimenArrayType()
	{
		return specimenArrayType;
	}

	/**
	 * @param specimenArrayType The specimenArrayType to set.
	 */
	public void setSpecimenArrayType(SpecimenArrayType specimenArrayType)
	{
		this.specimenArrayType = specimenArrayType;
	}

	//    /**
	//     * @return Returns the storageContainer.
	//     * @hibernate.many-to-one column="STORAGE_CONTAINER_ID" class="edu.wustl.
	//	   * catissuecore.domain.StorageContainer"
	//     * constrained="true"
	//     */
	//    public StorageContainer getStorageContainer()
	//    {
	//        return storageContainer;
	//    }
	//
	//    /**
	//     * @param storageContainer The storageContainer to set.
	//     */
	//    public void setStorageContainer(StorageContainer storageContainer)
	//    {
	//        this.storageContainer = storageContainer;
	//    }

	/**
	 * @return Returns the available.
	 * @hibernate.property name="available" type="boolean" column="AVAILABLE"
	 */
	public Boolean getAvailable()
	{
		return available;
	}

	/**
	 * @param available The available to set.
	 */
	public void setAvailable(Boolean available)
	{
		this.available = available;
	}

	/**
	 * Set All Values.
	 * @see edu.wustl.common.domain.AbstractDomainObject#setAllValues(edu.wustl.
	 * common.actionForm.AbstractActionForm)
	 * @param actionForm IValueObject.
	 * @throws AssignDataException AssignDataException.
	 */
	public void setAllValues(IValueObject actionForm) throws AssignDataException
	{
		try
		{
			if (SearchUtil.isNullobject(specimenArrayType))
			{
				specimenArrayType = new SpecimenArrayType();
			}
			if (SearchUtil.isNullobject(createdBy))
			{
				createdBy = new User();
			}
			if (SearchUtil.isNullobject(locatedAtPosition))
			{
				locatedAtPosition = new ContainerPosition();
			}
			if (SearchUtil.isNullobject(locatedAtPosition.parentContainer))
			{
				locatedAtPosition.parentContainer = new StorageContainer();
			}
			if (SearchUtil.isNullobject(available))
			{
				available = Boolean.TRUE;
			}
			if (actionForm instanceof SpecimenArrayAliquotForm)
			{
				SpecimenArrayAliquotForm form = (SpecimenArrayAliquotForm) actionForm;
				this.aliqoutMap = form.getSpecimenArrayAliquotMap();
				this.aliquotCount = Integer.parseInt(form.getAliquotCount());
				this.id = Long.valueOf(form.getSpecimenArrayId());
			}
			else
			{
				super.setAllValues(actionForm);
				SpecimenArrayForm specimenArrayForm = (SpecimenArrayForm) actionForm;
				specimenArrayType.setId(Long.valueOf(specimenArrayForm.getSpecimenArrayTypeId()));

				if (this.locatedAtPosition == null)
				{
					this.locatedAtPosition = new ContainerPosition();
				}
				if (specimenArrayForm.getStContSelection() == 1)
				{
					this.locatedAtPosition.parentContainer.setId(Long.valueOf(specimenArrayForm
							.getStorageContainer()));

					this.locatedAtPosition.positionDimensionOne = Integer.valueOf(specimenArrayForm
							.getPositionDimensionOne());
					this.locatedAtPosition.positionDimensionTwo = Integer.valueOf(specimenArrayForm
							.getPositionDimensionTwo());
					this.locatedAtPosition.occupiedContainer = this;

				}
				else
				{
					this.locatedAtPosition.parentContainer.setName(specimenArrayForm
							.getSelectedContainerName());
					if (specimenArrayForm.getPos1() != null
							&& !specimenArrayForm.getPos1().trim().equals(Constants.DOUBLE_QUOTES)
							&& specimenArrayForm.getPos2() != null
							&& !specimenArrayForm.getPos2().trim().equals(""))
					{
						this.locatedAtPosition.positionDimensionOne = Integer
								.valueOf(specimenArrayForm.getPos1());
						this.locatedAtPosition.positionDimensionTwo = Integer
								.valueOf(specimenArrayForm.getPos2());
						this.locatedAtPosition.occupiedContainer = this;
					}
				}
				if (createdBy == null)
				{
					createdBy = new User();
				}
				createdBy.setId(Long.valueOf(specimenArrayForm.getCreatedBy()));
				// done in Container class
				/*
				capacity.setOneDimensionCapacity(new Integer(specimenArrayForm.getOneDimensionCapacity()));
				capacity.setTwoDimensionCapacity(new Integer(specimenArrayForm.getTwoDimensionCapacity()));
				*/
				specimenArrayContentCollection = specimenArrayForm.getSpecArrayContentCollection();
				//			SpecimenArrayUtil.getSpecimenContentCollection(specimenArrayForm.
				//			getSpecimenArrayGridContentList());

				//Ordering System
				if (specimenArrayForm.getIsDefinedArray().equalsIgnoreCase("True"))
				{
					SpecimenArrayBizLogic specimenArrayBizLogic = (SpecimenArrayBizLogic) BizLogicFactory
							.getInstance().getBizLogic(Constants.SPECIMEN_ARRAY_FORM_ID);
					NewSpecimenArrayOrderItem newSpecimenArrayOrderItem = specimenArrayBizLogic
							.getNewSpecimenArrayOrderItem(Long.valueOf(specimenArrayForm
									.getNewArrayOrderItemId()));
					Collection tempColl = new HashSet();
					tempColl.add(newSpecimenArrayOrderItem);
					this.newSpecimenArrayOrderItemCollection = tempColl;
					newSpecimenArrayOrderItem.setSpecimenArray(this);
				}
			}
		}
		catch (BizLogicException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (NumberFormatException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * @return Returns the aliqoutMap.
	 */
	public Map getAliqoutMap()
	{
		return aliqoutMap;
	}

	/**
	 * @param aliqoutMap The aliqoutMap to set.
	 */
	public void setAliqoutMap(Map aliqoutMap)
	{
		this.aliqoutMap = aliqoutMap;
	}

	/**
	 * @return Returns the aliquotCount.
	 */
	public int getAliquotCount()
	{
		return aliquotCount;
	}

	/**
	 * @param aliquotCount The aliquotCount to set.
	 */
	public void setAliquotCount(int aliquotCount)
	{
		this.aliquotCount = aliquotCount;
	}

	/**
	 * @return Returns the aliquot.
	 */
	public boolean isAliquot()
	{
		return aliquot;
	}

	/**
	 * @param aliquot The aliquot to set.
	 */
	public void setAliquot(boolean aliquot)
	{
		this.aliquot = aliquot;
	}
}