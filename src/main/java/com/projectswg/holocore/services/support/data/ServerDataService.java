package com.projectswg.holocore.services.support.data;

import com.projectswg.holocore.resources.support.data.server_info.mongodb.PswgDatabase;
import me.joshlarson.jlcommon.control.Service;
import me.joshlarson.jlcommon.log.Log;

public class ServerDataService extends Service {
	
	public ServerDataService() {
		
	}
	
	@Override
	public boolean initialize() {
		if (PswgDatabase.INSTANCE.getConfig().getBoolean(this, "wipeObjects", false)) {
			Log.d("Cleared %d objects", PswgDatabase.INSTANCE.getObjects().clearObjects());
		}
		
		return super.initialize();
	}
	
}
