/*******************************************************************************
 * Copyright (c) 2014 Institute for Pervasive Computing, ETH Zurich and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 * 
 * The Eclipse Public License is available at
 *    http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *    http://www.eclipse.org/org/documents/edl-v10.html.
 * 
 * Contributors:
 *    Matthias Kovatsch - creator and main architect
 *    Stefan Jucker - DTLS implementation
 ******************************************************************************/
package org.eclipse.californium.scandium.dtls;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.californium.scandium.dtls.cipher.ECDHECryptography.SupportedGroup;
import org.eclipse.californium.scandium.util.DatagramReader;
import org.eclipse.californium.scandium.util.DatagramWriter;


/**
 * The supported elliptic curves extension.
 * 
 * For details see <a href="http://tools.ietf.org/html/rfc4492#section-5.1.1">
 * RFC 4492</a>.
 */
public final class SupportedEllipticCurvesExtension extends HelloExtension {

	// DTLS-specific constants ////////////////////////////////////////

	private static final int LIST_LENGTH_BITS = 16;

	private static final int CURVE_BITS = 16;
	
	// Members ////////////////////////////////////////////////////////
	
	/** The list holding the supported named curves IDs */
	private final List<Integer> ellipticCurveList;
	
	// Constructor ////////////////////////////////////////////////////

	/**
	 * 
	 * @param ellipticCurveList
	 *            the list of supported named curves.
	 */
	public SupportedEllipticCurvesExtension(List<Integer> ellipticCurveList) {
		super(ExtensionType.ELLIPTIC_CURVES);
		this.ellipticCurveList = ellipticCurveList;
	}
	
	// Serialization //////////////////////////////////////////////////

	@Override
	public byte[] toByteArray() {
		DatagramWriter writer = new DatagramWriter();
		writer.writeBytes(super.toByteArray());

		int listLength = ellipticCurveList.size() * 2;
		writer.write(listLength + 2, LENGTH_BITS);
		writer.write(listLength, LIST_LENGTH_BITS);

		for (Integer curveId : ellipticCurveList) {
			writer.write(curveId, CURVE_BITS);
		}

		return writer.toByteArray();
	}

	public static HelloExtension fromByteArray(byte[] byteArray) {
		DatagramReader reader = new DatagramReader(byteArray);

		int listLength = reader.read(LIST_LENGTH_BITS);

		List<Integer> ellipticCurveList = new ArrayList<Integer>();
		while (listLength > 0) {
			int id = reader.read(CURVE_BITS);
			ellipticCurveList.add(id);

			listLength -= 2;
		}

		return new SupportedEllipticCurvesExtension(ellipticCurveList);
	}
	
	// Methods ////////////////////////////////////////////////////////
	
	@Override
	public int getLength() {
		// fixed: type (2 bytes), length (2 bytes), list length (2 bytes)
		// variable: number of named curves * 2 (2 bytes for each curve)
		return 6 + (ellipticCurveList.size() * 2);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(super.toString());
		sb.append("\t\t\t\tLength: ").append(getLength() - 4).append("\n");
		sb.append("\t\t\t\tElliptic Curves Length: ").append(getLength() - 6).append("\n");
		sb.append("\t\t\t\tElliptic Curves (").append(ellipticCurveList.size()).append(" curves):\n");

		for (Integer curveId : ellipticCurveList) {
			SupportedGroup group = SupportedGroup.fromId(curveId);
			if (group != null) {
				sb.append("\t\t\t\t\tElliptic Curve: ").append(group.name());
			} else {
				sb.append("\t\t\t\t\tElliptic Curve: unknown");
			}
			sb.append(" (").append(curveId).append(")\n");
		}

		return sb.toString();
	}

	public List<Integer> getEllipticCurveList() {
		return Collections.unmodifiableList(ellipticCurveList);
	}

}
