/**
 * 
 */
package org.easycluster.easycluster.cluster.netty;

import java.util.ArrayList;

import org.easycluster.easycluster.serialization.bytebean.annotation.ByteField;
import org.easycluster.easycluster.serialization.protocol.annotation.SignalCode;
import org.easycluster.easycluster.serialization.protocol.xip.AbstractXipResponse;

@SignalCode(messageCode = 2080001)
public class GetBagItemsResp extends AbstractXipResponse {

	@ByteField(index = 2, description = "列表信息")
	private ArrayList<BagItemInfo>	bagItemList	= new ArrayList<BagItemInfo>();

	@ByteField(index = 3, description = "玩家己开启的格子数")
	private int						gridNum;

	@ByteField(index = 4, bytes = 1, description = " 1表示为背包  2 表示为仓库 3 人物信息")
	private int						type;

	public ArrayList<BagItemInfo> getBagItemList() {
		return bagItemList;
	}

	public void setBagItemList(ArrayList<BagItemInfo> bagItemList) {
		this.bagItemList = bagItemList;
	}

	public int getGridNum() {
		return gridNum;
	}

	public void setGridNum(int gridNum) {
		this.gridNum = gridNum;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

}
