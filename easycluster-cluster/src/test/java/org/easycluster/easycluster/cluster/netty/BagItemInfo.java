/**
 * 
 */
package org.easycluster.easycluster.cluster.netty;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.easycluster.easycluster.serialization.bytebean.ByteBean;
import org.easycluster.easycluster.serialization.bytebean.annotation.ByteField;

public class BagItemInfo implements ByteBean {

	@ByteField(index = 0, description = "第几个格子")
	private int		gridNo;

	@ByteField(index = 1, description = "物品的id")
	private long	objectId;

	@ByteField(index = 2, description = "物品的数量")
	private int		itemNum;

	@ByteField(index = 3, description = "强化的等级")
	private int		strengthenLevel;

	@ByteField(index = 4, description = "物品id")
	private int		itemId;

	@ByteField(index = 5, description = "是否绑定")
	private boolean	isBind;

	@ByteField(index = 6, bytes = 1, description = " 1表示为背包  2 表示为仓库 3 人物信息")
	private int		type;

	@ByteField(index = 7, description = "镶嵌宝石的id")
	private String	inlayStone;

	@ByteField(index = 8, description = "强化系数")
	private float	strengthenGrowth;

	/**
	 * @return the type
	 */
	public int getType() {
		return type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(int type) {
		this.type = type;
	}

	public boolean isBind() {
		return isBind;
	}

	public void setBind(boolean isBind) {
		this.isBind = isBind;
	}

	public int getItemId() {
		return itemId;
	}

	public void setItemId(int itemId) {
		this.itemId = itemId;
	}

	public int getGridNo() {
		return gridNo;
	}

	public void setGridNo(int gridNo) {
		this.gridNo = gridNo;
	}

	public long getObjectId() {
		return objectId;
	}

	public void setObjectId(long objectId) {
		this.objectId = objectId;
	}

	public int getItemNum() {
		return itemNum;
	}

	public void setItemNum(int itemNum) {
		this.itemNum = itemNum;
	}

	/**
	 * @return the strengthenLevel
	 */
	public int getStrengthenLevel() {
		return strengthenLevel;
	}

	/**
	 * @param strengthenLevel
	 *            the strengthenLevel to set
	 */
	public void setStrengthenLevel(int strengthenLevel) {
		this.strengthenLevel = strengthenLevel;
	}

	public String getInlayStone() {
		return inlayStone;
	}

	public void setInlayStone(String inlayStone) {
		this.inlayStone = inlayStone;
	}

	public float getStrengthenGrowth() {
		return strengthenGrowth;
	}

	public void setStrengthenGrowth(float strengthenGrowth) {
		this.strengthenGrowth = strengthenGrowth;
	}

	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

}
