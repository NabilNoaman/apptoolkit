package com.mcxiaoke.shell.others;

public class ProcessUtils {

/*	public static List<MemProcessInfo> getUserProcessList() {
		return getProcessList();
	}

	private static List<MemProcessInfo> getProcessList() {

		List<MemProcessInfo> ret = null;

		CommandResult result = RootUtils.runCommand("toolbox ps", false, null);
		int position = 0;
		if (result != null) {
			if (result.error.equals("")) {
				String r = result.result;
				r = r.toLowerCase();
				// r = r.replaceAll("\\s+", " ");
				String[] ss = r.split("\n");
				ret = new ArrayList<MemProcessInfo>();
				for (int i = 1; i < ss.length; i++) {
					if (!ss[i].startsWith("root")) {
						MemProcessInfo info = null;
						try {
							info = MemProcessInfo.stringToProcessInfo(ss[i]);
						} catch (Exception e) {
							info = null;
						}

						if (info == null) {
							continue;
						}
						if (info.PID > 127) {
							info.position = position;
							ret.add(info);
							position++;
						}
					}
				}
			}
		}
		return ret;
	}*/
}
