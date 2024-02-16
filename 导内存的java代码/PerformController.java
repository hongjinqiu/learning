package com.onlyou.malay.acct.sales.web;

import com.onlyou.framework.adapter.vo.ResponseResult;
import com.onlyou.framework.base.utils.BigDecimalUtils;
import com.onlyou.framework.base.utils.JSONUtils;
import com.onlyou.framework.base.utils.ListUtils;
import com.onlyou.framework.base.utils.MapUtils;
import com.onlyou.framework.base.utils.StringUtils;
import com.onlyou.framework.base.utils.UUIDUtils;
import com.onlyou.framework.constant.StringConstant;
import com.onlyou.framework.exception.ExtBusinessException;
import com.onlyou.framework.mybatis.dao.pojo.Page;
import com.onlyou.framework.spring.context.utils.SpringContextUtil;
import com.onlyou.framework.spring.web.bind.annotation.JsonPathParam;
import com.onlyou.framework.tool.pub.PubUtil;
import com.onlyou.malay.acct.bd.service.IInventoryService;
import com.onlyou.malay.acct.bd.vo.InventoryLastPriceVo;
import com.onlyou.malay.acct.commom.web.AbstractSaleBillController;
import com.onlyou.malay.acct.common.constant.AcctConstants;
import com.onlyou.malay.acct.common.constant.AcctParamConstants;
import com.onlyou.malay.acct.common.constant.AcctParamEnum;
import com.onlyou.malay.acct.common.enumerate.AcctPartnerTypeEnum;
import com.onlyou.malay.acct.common.enumerate.ActionNameEnum;
import com.onlyou.malay.acct.common.enumerate.BillTypeEnum;
import com.onlyou.malay.acct.common.enumerate.ExportTypeEnum;
import com.onlyou.malay.acct.common.enumerate.GeneralMethodEnum;
import com.onlyou.malay.acct.common.enumerate.SaleTypeEnum;
import com.onlyou.malay.acct.common.vo.AcctResponseResult;
import com.onlyou.malay.acct.common.vo.AcctUserInfo;
import com.onlyou.malay.acct.common.vo.IDocBillVo;
import com.onlyou.malay.acct.common.vo.RefBillSearchVo;
import com.onlyou.malay.acct.partner.service.IAcctPartnerService;
import com.onlyou.malay.acct.partner.vo.AcctPartnerInfo;
import com.onlyou.malay.acct.report.util.ReportUtils;
import com.onlyou.malay.acct.sales.pdf.AbstractSalOrPurPdf;
import com.onlyou.malay.acct.sales.pdf.SaleInvoiceClassicPdf;
import com.onlyou.malay.acct.sales.pdf.SaleInvoiceSimplePdf;
import com.onlyou.malay.acct.sales.pdf.SaleInvoiceStandardPdf;
import com.onlyou.malay.acct.sales.service.IBillCustomInfoService;
import com.onlyou.malay.acct.sales.service.IMemoryOutput;
import com.onlyou.malay.acct.sales.service.ISaleInvoiceService;
import com.onlyou.malay.acct.sales.service.ITwentyOneRulesService;
import com.onlyou.malay.acct.sales.vo.SaleInvoiceBVo;
import com.onlyou.malay.acct.sales.vo.SaleInvoiceBillVo;
import com.onlyou.malay.acct.sales.vo.SaleInvoiceExportVo;
import com.onlyou.malay.acct.sales.vo.SaleInvoiceSearchVo;
import com.onlyou.malay.acct.sales.vo.SaleInvoiceVo;
import com.onlyou.malay.acct.setup.entity.AcctBuildEntity;
import com.onlyou.malay.acct.setup.entity.AcctParamEntity;
import com.onlyou.malay.acct.setup.entity.CurrencyEntity;
import com.onlyou.malay.acct.setup.service.ICurrencyService;
import com.onlyou.malay.acct.system.vo.SendMailVo;
import com.onlyou.malay.acct.vop.image.vo.ImageVo;
import com.onlyou.malay.acct.vop.service.IVopCorpEntityService;
import com.onlyou.malay.acct.voucher.entity.VoucherImageRefEntity;
import com.onlyou.malay.application.utils.I18nMessageUtil;
import com.onlyou.malay.vop.common.export.ExportUtil;
import com.onlyou.malay.vop.common.util.MapUtil;
import com.onlyou.malay.vop.cor.entity.CorpEntity;
import net.minidev.json.JSONObject;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 销售发票Controller
 *
 * @author yxzhang
 */
@Controller
@RequestMapping("/acct/sales/performController")
public class PerformController extends AbstractSaleBillController {
//	private int runCount = 3;
//	private int collectPerRunCount = 10;
//	private int threadCount = 1;
	@Autowired
	protected IMemoryOutput memoryOutput;

	@RequestMapping("/moreMemoryTest.json")
	@ResponseBody
	public ResponseResult moreMemoryTest(HttpServletRequest request) {
		ResponseResult result = new ResponseResult();
		try {
			String pid = memoryOutput.getPid();
			for (int i = 0; i < 6; i++) {
				String file = "E:\\hongjinqiu\\tmp\\single\\begin_" + i + ".bin";
//				memoryOutput.dump(file);
				dump(file, pid);

				SaleInvoiceSearchVo searchVo = new SaleInvoiceSearchVo();
				searchVo.setBeginBillDate(new Date(1522512000000L));
				searchVo.setEndBillDate(new Date(1525017600000L));
				searchVo.setDateCheckbox("1");

				Page page = new Page();
				page.setPageSize(10);
				page.setCurrentPage(1);
				String orderByName = "";
				String orderByType = "";

				searchVo.setCorpId(getCorpId(request));
				if(StringUtils.isNotBlank(orderByName)){
					searchVo.setOrderByName(orderByName);
				}
				if(StringUtils.isNotBlank(orderByType)){
					searchVo.setOrderByType(orderByType);
				}
				file = "E:\\hongjinqiu\\tmp\\single\\begin_" + i + "_end.bin";
				long begin = System.currentTimeMillis();
				SaleInvoiceController saleInvoiceController = (SaleInvoiceController) SpringContextUtil.getBean("saleInvoiceController");
				Page resultPage = saleInvoiceController.query(searchVo, page, request, "", "");
				long end = System.currentTimeMillis();
//			Page resultPage = memoryOutput.querySaleInvoiceVoPage(file, BillTypeEnum.SALE_INVOICE.getCode(), searchVo, page, getUserInfo(request));
				System.out.println(resultPage);
//				memoryOutput.dump(file);
				dump(file, pid);
			}

			result.setInfo("performance");
		} catch (Exception e) {
			result.setSuccess(false);
			result.setMessage(e.getMessage());
		}
		return result;
	}

	private String dump(String file, String pid) throws Exception {
		ProcessBuilder pb = new ProcessBuilder("cmd", "/c", "jmap", String.format("-dump:format=b,file=%s", file), pid);
		// 如果将值设置为 true，标准错误将与标准输出合并。这使得关联错误消息和相应的输出变得更容易。
		// 在此情况下，合并的数据可从 Process.getInputStream() 返回的流读取
		pb.redirectErrorStream(true);
		Process ps = pb.start();
		Scanner scanner = new Scanner(ps.getInputStream());
		StringBuilder result = new StringBuilder();
		while (scanner.hasNextLine()) {
			result.append(scanner.nextLine());
			result.append(System.getProperty("line.separator"));
		}

		scanner.close();
		ps.destroy();
		return result.toString();
	}

    @RequestMapping("/performance.json")
	@ResponseBody
	public ResponseResult performance(HttpServletRequest request) {
    	ResponseResult result = new ResponseResult();
    	try {
			String file = "E:\\hongjinqiu\\tmp\\single\\heap1.bin";
			memoryOutput.dump(file);

			SaleInvoiceSearchVo searchVo = new SaleInvoiceSearchVo();
			searchVo.setBeginBillDate(new Date(1522512000000L));
			searchVo.setEndBillDate(new Date(1525017600000L));
			searchVo.setDateCheckbox("1");

			Page page = new Page();
			page.setPageSize(10);
			page.setCurrentPage(1);
			String orderByName = "";
			String orderByType = "";

			searchVo.setCorpId(getCorpId(request));
			if(StringUtils.isNotBlank(orderByName)){
				searchVo.setOrderByName(orderByName);
			}
			if(StringUtils.isNotBlank(orderByType)){
				searchVo.setOrderByType(orderByType);
			}
			file = "E:\\hongjinqiu\\tmp\\single\\heap2.bin";
			Page resultPage = memoryOutput.querySaleInvoiceVoPage(file, BillTypeEnum.SALE_INVOICE.getCode(), searchVo, page, getUserInfo(request));
			System.out.println(resultPage);

			result.setInfo("performance");
		} catch (Exception e) {
    		result.setSuccess(false);
    		result.setMessage(e.getMessage());
		}
		return result;
	}

	@RequestMapping("/performanceMulti.json")
	@ResponseBody
	public ResponseResult performanceMulti(final HttpServletRequest request) {
		final int runCount = 1500;
		final int threadCount = 2;

		ResponseResult result = new ResponseResult();
		try {
			long totalBegin = System.currentTimeMillis();
			final List<Long> spendMetrics = new CopyOnWriteArrayList<>();

			final CountDownLatch countDownLatch = new CountDownLatch(runCount * threadCount);
			ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

			for (int i = 0; i < runCount; i++) {
				for (int j = 0; j < threadCount; j++) {
					final int printI = i;
					final int printJ = j;
					executorService.execute(new Thread(){

						@Override
						public void run() {
							SaleInvoiceSearchVo searchVo = new SaleInvoiceSearchVo();
							searchVo.setBeginBillDate(new Date(1522512000000L));
							searchVo.setEndBillDate(new Date(1522511000000L));
							searchVo.setDateCheckbox("1");

							Page page = new Page();
							page.setPageSize(10);
							page.setCurrentPage(1);

							long begin = System.currentTimeMillis();
							SaleInvoiceController saleInvoiceController = (SaleInvoiceController) SpringContextUtil.getBean("saleInvoiceController");
							Page resultPage = saleInvoiceController.query(searchVo, page, request, "", "");
							long end = System.currentTimeMillis();

							spendMetrics.add(end - begin);
							countDownLatch.countDown();
							if (printI % 100 == 0) {
								System.out.println("\nrunCount is:" + printI + ", threadCount is:" + printJ);
							}
						}
					});
				}
			}
			countDownLatch.await();
			outputMetrics(spendMetrics);

			long totalEnd = System.currentTimeMillis();
			result.setInfo("test by me, spend is:" + ((totalEnd - totalBegin) / 1000.0));
		} catch (Exception e) {
			result.setSuccess(false);
			result.setMessage(e.getMessage());
		}
		return result;
	}

	private void outputMetrics(List<Long> spendMetrics) throws IOException {
    	String content = StringUtils.join(spendMetrics.toArray(), "\r\n");
		FileOutputStream out = null;
		try {
			out = new FileOutputStream("e:\\hongjinqiu\\tmp\\single\\metrics1.txt");
			out.write(content.getBytes());
		} finally {
			IOUtils.closeQuietly(out);
		}
	}

	@Override
	protected String getBillType() {
		return null;
	}

	@Override
	protected Class getBillVoClass() {
		return null;
	}

	@Override
	protected String[] getHeadQueryExprs() {
		return new String[0];
	}

	@Override
	protected String[] getBodyQueryExprs() {
		return new String[0];
	}
}
