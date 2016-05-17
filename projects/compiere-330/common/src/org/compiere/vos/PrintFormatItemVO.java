package org.compiere.vos;

public class PrintFormatItemVO  extends ResponseVO implements Comparable<PrintFormatItemVO>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public PrintFormatItemVO(){
		
	}
	
	private String name,printText,isActive,isPrinted,fieldGroupName,isOrderBy,isGroupBy,isAveraged,isCounted,isDeviationCalc,isMaxCalc,isMinCalc,IsSummarized,IsVarianceCalc;
	
	private int recordSortNumber,adFieldGroupID,printFormatItemID,seqNo;	

	public int getAdFieldGroupID() {
		return adFieldGroupID;
	}

	public void setAdFieldGroupID(int adFieldGroupID) {
		this.adFieldGroupID = adFieldGroupID;
	}

	public String getIsPrinted() {
		return isPrinted;
	}

	public void setIsPrinted(String isPrinted) {
		this.isPrinted = isPrinted;
	}

	public int getPrintFormatItemID() {
		return printFormatItemID;
	}

	public void setPrintFormatItemID(int printFormatItemID) {
		this.printFormatItemID = printFormatItemID;
	}	

	public String getIsActive() {
		return isActive;
	}

	public void setIsActive(String isActive) {
		this.isActive = isActive;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}	

	public String getPrintText() {
		return printText;
	}

	public void setPrintText(String printText) {
		this.printText = printText;
	}

	public int getRecordSortNumber() {
		return recordSortNumber;
	}

	public void setRecordSortNumber(int recordSortNumber) {
		this.recordSortNumber = recordSortNumber;
	}	

	public String getFieldGroupName() {
		return fieldGroupName;
	}

	public void setFieldGroupName(String fieldGroupName) {
		this.fieldGroupName = fieldGroupName;
	}
	
	public String toString(){
		return getName();
	}

	public String getIsOrderBy() {
		return isOrderBy;
	}

	public void setIsOrderBy(String isOrderBy) {
		this.isOrderBy = isOrderBy;
	}

	public int getSeqNo() {
		return seqNo;
	}

	public void setSeqNo(int seqNo) {
		this.seqNo = seqNo;
	}
	
	public int compareTo(PrintFormatItemVO o) {
		if (this.getAdFieldGroupID() != o.getAdFieldGroupID())
			return (this.getAdFieldGroupID()-o.getAdFieldGroupID());
		else if (this.seqNo != o.getSeqNo())
			return (this.getSeqNo()-o.getSeqNo());
		else
			return (this.getName().compareTo(o.getName()));		
	}

	public String getIsGroupBy() {
		return isGroupBy;
	}

	public void setIsGroupBy(String isGroupBy) {
		this.isGroupBy = isGroupBy;
	}

	public String getIsAveraged() {
		return isAveraged;
	}

	public void setIsAveraged(String isAveraged) {
		this.isAveraged = isAveraged;
	}

	public String getIsCounted() {
		return isCounted;
	}

	public void setIsCounted(String isCounted) {
		this.isCounted = isCounted;
	}

	public String getIsDeviationCalc() {
		return isDeviationCalc;
	}

	public void setIsDeviationCalc(String isDeviationCalc) {
		this.isDeviationCalc = isDeviationCalc;
	}

	public String getIsMaxCalc() {
		return isMaxCalc;
	}

	public void setIsMaxCalc(String isMaxCalc) {
		this.isMaxCalc = isMaxCalc;
	}

	public String getIsMinCalc() {
		return isMinCalc;
	}

	public void setIsMinCalc(String isMinCalc) {
		this.isMinCalc = isMinCalc;
	}

	public String getIsSummarized() {
		return IsSummarized;
	}

	public void setIsSummarized(String isSummarized) {
		IsSummarized = isSummarized;
	}

	public String getIsVarianceCalc() {
		return IsVarianceCalc;
	}

	public void setIsVarianceCalc(String isVarianceCalc) {
		IsVarianceCalc = isVarianceCalc;
	}
}
