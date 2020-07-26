package com.PocketCare.pocketCare.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.PocketCare.pocketCare.DAO.UserDataDAO;
import com.PocketCare.pocketCare.DAO.UserSymptomsDao;
import com.PocketCare.pocketCare.Entities.UserSymptons;
import com.PocketCare.pocketCare.Exception.CustomException;
import com.PocketCare.pocketCare.Utils.AppUtils;
import com.PocketCare.pocketCare.model.HealthAnalyticsInfo;
import com.PocketCare.pocketCare.model.HealthAnalyticsResponse;

@Service
public class HealthAnalytics {
	
	@Autowired
	UserSymptomsDao userSymptomsDao;
	
	@Autowired
	UserDataDAO userDao;
	
	public HealthAnalyticsResponse getSummary(long startDate, long endDate) throws CustomException {
		 List<Long> dates = AppUtils.getDates(startDate, endDate);
		 long totalUsers = userDao.getUserDataCount();
		 Map<Long, HealthAnalyticsInfo> map = new TreeMap<>();
		 for(Long date: dates) {
			 long healthCount = userSymptomsDao.getHealthyCounts(date);
			 long totalFilled = userSymptomsDao.getTotalFilled(date);
			 List<UserSymptons> unHealthyinfo = userSymptomsDao.getUnHealthInfo(date);
			 long unHealthyCount = (unHealthyinfo!=null)?unHealthyinfo.size():0;
			 Map<String, Integer> symptomsCount = getSymptomsCount(unHealthyinfo);
			 HealthAnalyticsInfo currentInfo = new HealthAnalyticsInfo(totalFilled, healthCount, unHealthyCount, totalUsers,symptomsCount);
			 map.put(date, currentInfo);
		 } 
		 HealthAnalyticsResponse response = new HealthAnalyticsResponse();
		 response.setDateWiseHealthAnalytics(map);
		 return response;
		
	}
	
	private Map<String, Integer> getSymptomsCount(List<UserSymptons> userSymptoms){
		Map<String, Integer> map = new HashMap<>();
		if(Objects.isNull(userSymptoms)) {
			return map;
		}
		for(UserSymptons ele: userSymptoms) {
			List<String> symptoms = ele.getUsersSymptoms();
			for(String symptom: symptoms) {
				map.put(symptom, map.getOrDefault(symptom, 0)+1);
			}
		}
		
		return map;
	}

}
