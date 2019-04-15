package com.jule.domino.log.timer;

import com.jule.domino.log.db.CommonDAO;

import java.util.TimerTask;


public class CommitLogTask extends TimerTask{

	@Override
	public void run() {
		CommonDAO.OBJ.commitAllLogs();
	}

}
