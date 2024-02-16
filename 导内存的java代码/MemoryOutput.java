package com.onlyou.malay.acct.sales.service;

import com.onlyou.framework.exception.ExtBusinessException;
import com.onlyou.framework.mybatis.dao.pojo.Page;
import com.onlyou.malay.acct.common.vo.AcctUserInfo;
import com.onlyou.malay.acct.sales.vo.SaleInvoiceSearchVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.Scanner;

/**
 * 销售发票service实现
 *
 * @author zhangyx
 */
@Service
@Transactional
public class MemoryOutput implements IMemoryOutput {
	@Autowired
	private ISaleInvoiceService saleInvoiceService;

	@Override
	public String dump(String file) throws Exception {
		ProcessBuilder pb = new ProcessBuilder("cmd", "/c", "jmap", String.format("-dump:format=b,file=%s", file), getPid());
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

	@Override
	public Page querySaleInvoiceVoPage(String file, String billType, SaleInvoiceSearchVo condition, Page page, AcctUserInfo acctUserInfo) throws ExtBusinessException {
		Page result = saleInvoiceService.querySaleInvoiceVoPage(billType, condition, page, acctUserInfo);
		try {
			dump(file);
		} catch (Exception e) {
			throw new ExtBusinessException(e);
		}
		return result;
	}

	@Override
	public String getPid() {
		RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
		String name = runtime.getName(); // format: "pid@hostname"
		try {
			return name.substring(0, name.indexOf('@'));
		} catch (Exception e) {
			return "-1";
		}
	}
}
