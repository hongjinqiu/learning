package com.onlyou.malay.acct.sales.service;

import com.onlyou.framework.exception.ExtBusinessException;
import com.onlyou.framework.mybatis.dao.pojo.Page;
import com.onlyou.malay.acct.common.vo.AcctUserInfo;
import com.onlyou.malay.acct.sales.vo.SaleInvoiceSearchVo;

/**
 * 内存 dump 接口
 */
public interface IMemoryOutput {
	String getPid();

	String dump(String file) throws Exception;

	Page querySaleInvoiceVoPage(String file, String billType, SaleInvoiceSearchVo condition, Page page, AcctUserInfo acctUserInfo) throws ExtBusinessException;
}
