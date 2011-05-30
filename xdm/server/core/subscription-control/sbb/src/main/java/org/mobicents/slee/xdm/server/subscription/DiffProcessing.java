/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and/or its affiliates, and individual
 * contributors as indicated by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a full listing
 * of individual contributors.
 * 
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU General Public License, v. 2.0.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License,
 * v. 2.0 along with this distribution; if not, write to the Free 
 * Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301, USA.
 */
package org.mobicents.slee.xdm.server.subscription;

/**
 * @author baranowb
 * 
 */
public enum DiffProcessing {

	Aggregate("aggregate"), NoPatching("no-patching"), XcapPatching(
			"xcap-patching");

	/**
	 * string representing EventPackage header parameter with one of above
	 * values.
	 */
	public static final String PARAM = "diff-processing";

	private DiffProcessing(String t) {
		this.type = t;
	}

	private String type;

	public String toString() {
		return this.type;
	}

	public static DiffProcessing fromString(String type) {

		if (type == null) {
			return NoPatching; // default
		}

		if (type.equals(Aggregate.type)) {
			return Aggregate;
		}
		if (type.equals(NoPatching.type)) {
			return NoPatching;
		}
		if (type.equals(XcapPatching.type)) {
			return XcapPatching;
		}

		return NoPatching; // default or should it be null?

	}

}