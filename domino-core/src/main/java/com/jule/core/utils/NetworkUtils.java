/*
 * This file is part of aion-emu <aion-emu.com>.
 *
 * aion-emu is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * aion-emu is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with aion-emu.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jule.core.utils;

import lombok.extern.slf4j.Slf4j;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author KID, -Nemesiss-
 */
@Slf4j
public class NetworkUtils {

	/**
	 * check if IP address match pattern
	 * 
	 * @param pattern
	 *          *.*.*.* , 192.168.1.0-255 , *
	 * @param address
	 *          - 192.168.1.1<BR>
	 *          <code>address = 10.2.88.12  pattern = *.*.*.*   result: true<BR>
	 *                address = 10.2.88.12  pattern = *   result: true<BR>
	 *                address = 10.2.88.12  pattern = 10.2.88.12-13   result: true<BR>
	 *                address = 10.2.88.12  pattern = 10.2.88.13-125   result: false<BR></code>
	 * @return true if address match pattern
	 */
	public static boolean checkIPMatching(String pattern, String address) {
		if (pattern.equals("*.*.*.*") || pattern.equals("*"))
			return true;

		String[] mask = pattern.split("\\.");
		String[] ip_address = address.split("\\.");
		for (int i = 0; i < mask.length; i++) {
			if (mask[i].equals("*") || mask[i].equals(ip_address[i]))
				continue;
			else if (mask[i].contains("-")) {
				byte min = Byte.parseByte(mask[i].split("-")[0]);
				byte max = Byte.parseByte(mask[i].split("-")[1]);
				byte ip = Byte.parseByte(ip_address[i]);
				if (ip < min || ip > max)
					return false;
			}
			else
				return false;
		}
		return true;
	}
	/**
	 * MethodsTitle: 获取本机的IP
	 * @author: yb.l
	 * @date:
	 * @version
	 */
	public static Set<String> getLoalhostIP(){
		Set<String> ips = new HashSet<>();
		try {
			Enumeration<?> enumeration= NetworkInterface.getNetworkInterfaces();
			InetAddress ip=null;
			while(enumeration.hasMoreElements()){
				NetworkInterface netInterface = (NetworkInterface) enumeration.nextElement();
				Enumeration<?> addresses = netInterface.getInetAddresses();
				while (addresses.hasMoreElements()) {
					ip = (InetAddress) addresses.nextElement();
					System.out.println("服务地址:" + ip.getHostName());
					if (ip != null && ip instanceof Inet4Address){
						String ip1=ip.getHostAddress();
						System.out.println("本机所有的IP地址:"+ip1);
						ips.add(ip1);
					}
				}
			}
		} catch (SocketException e) {
			log.error(e.getMessage(),e);
		}
		return ips;
	}
}
