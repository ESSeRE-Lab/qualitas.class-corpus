package org.compiere.acct;

import java.math.*;
import java.sql.*;
import java.util.*;
import java.util.logging.*;

import org.compiere.model.*;
import org.compiere.util.*;



public class Doc_CostUpdate extends Doc
{

	private MProductCategory mpc = null;
	private MCostElement m_ce;
	private String costingMethod;
	private String costingLevel;
	MCostUpdate costupdate;


    public Doc_CostUpdate(MAcctSchema ass[], ResultSet rs, Trx trx)
    {
        super(ass, MCostUpdate.class, rs, MDocBaseType.DOCBASETYPE_StandardCostUpdate, trx);
    }

    @Override
	public String loadDocumentDetails()
    {
        costupdate = (MCostUpdate)getPO();
        if (costupdate.getM_Product_Category_ID()!=0)
        	mpc = MProductCategory.get(getCtx(), costupdate.getM_Product_Category_ID());
        
        p_lines = loadLines(costupdate);
		m_ce = MCostElement.getMaterialCostElement(MClient.get(getCtx()), X_C_AcctSchema.COSTINGMETHOD_StandardCosting);
        setDateAcct(costupdate.getDateAcct());
        setDateDoc(costupdate.getDateAcct());
        return null;
    }

    private DocLine[] loadLines(MCostUpdate costupdate)
    {
        ArrayList<DocLine> list = new ArrayList<DocLine>();
        MCostUpdateLine lines[] = costupdate.getLines();
        for(int i = 0; i < lines.length; i++)
        {
            MCostUpdateLine line = lines[i];
            DocLine docLine = new DocLine(line, this);
            list.add(docLine);
        }

        DocLine dl[] = new DocLine[list.size()];
        list.toArray(dl);
        return dl;
    }



    @Override
	public BigDecimal getBalance()
    {
        return Env.ZERO;
    }

    @Override
	public ArrayList<Fact> createFacts(MAcctSchema as)
    {
    	ArrayList<Fact> facts = new ArrayList<Fact>();
    	MProductCategoryAcct pca = null;
    	String costingMethodOfSchema = as.getCostingMethod();
    	String costingLevelOfSchema  = as.getCostingLevel();
    	
    	// Get the costing method and the costing level of the product Category for the current accounting schema.
    	if (mpc != null){
	    	pca = MProductCategoryAcct.get(getCtx(), mpc.getM_Product_Category_ID(), as.getC_AcctSchema_ID(), null);
	    	if (pca.getCostingMethod() != null)
	    		costingMethod = pca.getCostingMethod();
	    	else
	    		costingMethod = costingMethodOfSchema;
	    	
	    	if (pca.getCostingLevel() != null)
	    		costingLevel = pca.getCostingLevel();
	    	else
	    		costingLevel = costingLevelOfSchema;
	    	
	    	// proceed only if the costing method is standard
	    	if (!costingMethod.equals(X_C_AcctSchema.COSTINGMETHOD_StandardCosting))
	    		return facts;
    	}
    	
        Fact fact = new Fact(this, as, Fact.POST_Actual);

        FactLine dr = null;
		FactLine cr = null;

        for(int i = 0; i < p_lines.length; i++)
        {
        	DocLine line = p_lines[i];
        	
        	if (mpc == null){
        		pca = MProductCategoryAcct.get(getCtx(), MProduct.get(getCtx(), line.getM_Product_ID()).
        			                           getM_Product_Category_ID(), 
        			                           as.getC_AcctSchema_ID(), null);
    	    	if (pca.getCostingMethod() != null)
    	    		costingMethod = pca.getCostingMethod();
    	    	else
    	    		costingMethod = costingMethodOfSchema;
    	    	
    	    	if (pca.getCostingLevel() != null)
    	    		costingLevel = pca.getCostingLevel();
    	    	else
    	    		costingLevel = costingLevelOfSchema;
    	    	
    	    	// proceed only if the costing method is standard
    	    	if (!costingMethod.equals(X_C_AcctSchema.COSTINGMETHOD_StandardCosting))
    	    		return facts;

        	}
        	

        	String sql = "SELECT * FROM M_Cost WHERE M_Product_ID = ?"
        	           + " AND C_AcctSchema_ID = ?"
        	           + " AND M_CostElement_ID = ?"
        	           + " AND (LastCostPrice-CurrentCostPrice) <> 0"
        	           + " AND CurrentQty <>0"
        	           + " AND M_CostType_ID = ?";
        	if(costingLevel.equals(X_C_AcctSchema.COSTINGLEVEL_Tenant))
        	{
        		sql += " AND AD_Org_ID = 0"
        			+ " AND M_AttributeSetInstance_ID  = 0";
        	}
        	else if (costingLevel.equals(X_C_AcctSchema.COSTINGLEVEL_Organization))
        		sql += " AND M_AttributeSetInstance_ID  = 0";
        	else if (costingLevel.equals(X_C_AcctSchema.COSTINGLEVEL_BatchLot))
        		sql += " AND AD_Org_ID = 0";
       	
    		PreparedStatement pstmt = null;
    		try
    		{
    			pstmt = DB.prepareStatement(sql, (Trx) null);
    			pstmt.setInt (1, line.getM_Product_ID());
    			pstmt.setInt (2, as.getC_AcctSchema_ID());
    			pstmt.setInt (3, m_ce.getM_CostElement_ID());
    			pstmt.setInt (4, as.getM_CostType_ID());
    			ResultSet rs = pstmt.executeQuery ();
    			while (rs.next ())
    			{
    				MCost cost = new MCost (getCtx(), rs, null);
    				loadDetails(cost,line);
		            BigDecimal amt = line.getAmtSource();
		            MAccount db_acct, cr_acct;
		            
		            /* Decide the Credit and Debit Accounts */
		            if(amt.signum() == 1)
		            {
		                db_acct = line.getAccount(ProductCost.ACCTTYPE_P_Asset, as);
		                cr_acct = line.getAccount(ProductCost.ACCTTYPE_P_CostAdjustment, as);
		            } else
		            {
		                cr_acct = line.getAccount(ProductCost.ACCTTYPE_P_Asset, as);
		                db_acct = line.getAccount(ProductCost.ACCTTYPE_P_CostAdjustment, as);
		            }
		            
		            /* Create Credit and Debit lines*/ 
		            dr = fact.createLine(line, db_acct, as.getC_Currency_ID(), amt.abs(), null);
		            if (dr == null)
		            {
		            	p_Error = "No Product Costs";
		            	return null;
		            }
		            
		            cr = fact.createLine(line, cr_acct, as.getC_Currency_ID(), null, amt.abs());
		            if (cr == null)
		            {
		            	p_Error = "No Product Costs";
		            	return null;
		            }
    			}
    			rs.close ();
    			pstmt.close ();
    			pstmt = null;
    		}
			catch (Exception e)
			{
				log.log (Level.SEVERE, sql, e);
			}
			try
			{
				if (pstmt != null)
					pstmt.close ();
				pstmt = null;
			}
			catch (Exception e)
			{
				pstmt = null;
			}
        }
        facts.add(fact);
	    return facts;
	}

    private void loadDetails(MCost cost, DocLine line)
    {

        BigDecimal Qty = cost.getCurrentQty();
        line.setQty(Qty, costupdate.isSOTrx());
        BigDecimal PriceCost = cost.getCurrentCostPrice().subtract(cost.getLastCostPrice());
        BigDecimal LineNetAmt = null;
        if(PriceCost != null && PriceCost.signum() != 0)
            LineNetAmt = Qty.multiply(PriceCost);
        else
            LineNetAmt = Env.ZERO;
        line.setAmount(LineNetAmt);
    }
}