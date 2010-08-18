package org.openxdm.xcap.server.slee.appusage.xcapcaps;

import javax.xml.validation.Schema;

import org.mobicents.xdm.server.appusage.AppUsage;
import org.mobicents.xdm.server.appusage.AppUsageFactory;

public class XCAPCapsAppUsageFactory implements AppUsageFactory {

	private Schema schema = null;
	
	public XCAPCapsAppUsageFactory(Schema schema) {
		this.schema = schema;
	}
	
	public AppUsage getAppUsageInstance() {
		return new XCAPCapsAppUsage(schema.newValidator());
	}

	public String getAppUsageId() {
		return XCAPCapsAppUsage.ID;
	}
	
}
