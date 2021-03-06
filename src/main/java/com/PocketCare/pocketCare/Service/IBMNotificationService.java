/*
 * Copyright 2020 University at Buffalo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.PocketCare.pocketCare.Service;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import com.PocketCare.pocketCare.DAO.UserDataDAO;
import com.PocketCare.pocketCare.model.NotificationDevicesResponse;
import com.ibm.mobilefirstplatform.serversdk.java.push.APNs;
import com.ibm.mobilefirstplatform.serversdk.java.push.APNs.Builder.APNSNotificationType;
import com.ibm.mobilefirstplatform.serversdk.java.push.FCM;
import com.ibm.mobilefirstplatform.serversdk.java.push.FCM.Builder.FCMPriority;
import com.ibm.mobilefirstplatform.serversdk.java.push.FCM.Builder.Visibility;
import com.ibm.mobilefirstplatform.serversdk.java.push.FCM.FCMLights;
import com.ibm.mobilefirstplatform.serversdk.java.push.FCM.FCMLights.Builder.FCMLED;
import com.ibm.mobilefirstplatform.serversdk.java.push.FCM.FCMStyle;
import com.ibm.mobilefirstplatform.serversdk.java.push.FCM.FCMStyle.Builder.FCMStyleTypes;
import com.ibm.mobilefirstplatform.serversdk.java.push.Message;
import com.ibm.mobilefirstplatform.serversdk.java.push.Notification;
import com.ibm.mobilefirstplatform.serversdk.java.push.PushNotifications;
import com.ibm.mobilefirstplatform.serversdk.java.push.PushNotificationsResponseListener;
import com.ibm.mobilefirstplatform.serversdk.java.push.Settings;
import com.ibm.mobilefirstplatform.serversdk.java.push.Target;

@Service
public class IBMNotificationService {

	private static final Logger logger = LogManager.getLogger(UserDataDAO.class);
	private static final String notificationMessage = "You may have been exposed to COVID-19 virus. Please follow these instructions as per https://www.cdc.gov/coronavirus/2019-nCoV/index.html";
	private static final String notificationURL = "https://www.cdc.gov/coronavirus/2019-nCoV/index.html";
	private static final String notificationTitle = "Important Notification";
	private static final String notificationSubTitle = "You have been exposed.";
	private static final String bigTextNotification = "You have been identified as close contact with a COVID-19 patient. Please follow these instructions as per https://www.cdc.gov/coronavirus/2019-nCoV/index.html";
	private static final String overrideHost= "https://imfpush.ng.bluemix.net";
	private static final String APIID= "YOUR IBM push notification APP ID";
	private static final String APIKEY = "Your IBM push notifcation API KEY";

	private Notification getNotification(List<String> deviceIdList){
		Notification notification = new Notification.Builder().message(getMessage()).settings(getSettings()).target(getTarget(deviceIdList)).build();
		return notification;
	}
	
	public NotificationDevicesResponse sendNotifcation(List<String> deviceIdList) {
		Notification ff = getNotification(deviceIdList);
		NotificationListner listner = new NotificationListner();
		PushNotifications.overrideServerHost = overrideHost;
		PushNotifications.initWithApiKey(APIID,APIKEY,PushNotifications.US_SOUTH_REGION); 
		PushNotifications.send(ff, listner);
		NotificationDevicesResponse response = new NotificationDevicesResponse();
		response.setStatusCode(listner.getStatusCode());
		response.setResponseBody(listner.getResponseBody());
		return response;
	}
	
	private Target getTarget(List<String> deviceIdlist) {
		String[] deviceIds = deviceIdlist.toArray(new String[0]);
		Target target = new Target.Builder()
				 .deviceIds(deviceIds)
				 .build();
		return target;
	}
	
	private Message getMessage() {
		// TODO Auto-generated method stub
		Message message = new Message.Builder().alert(notificationMessage).url(notificationURL).build();
		return message;
	}

	private Settings getSettings() {
		Settings settings = new Settings.Builder().apns(getAPNs()).fcm(getFCMs()).build();
		return settings;
	}
	
	private APNs getAPNs() {
		
		// For APNs settings.
	 	APNs apns = new APNs.Builder().badge(1).interactiveCategory("Accept")
	 		.iosActionKey("PUSH_OFFER").payload(new JSONObject().put("alert" , notificationMessage))
	 		.type(APNSNotificationType.DEFAULT).titleLocKey("ALERT").locKey("REPLYTO")
	 		.titleLocArgs(new String[] {"PocketCare S", "notification"})
	 		.locArgs(new String[] { "PocketCare S","notification" }).title(notificationTitle).subtitle(notificationSubTitle)
	 		.attachmentUrl(notificationURL)
	 		.build();
	 	return apns;
	}
	
	private FCM getFCMs() {
		FCMStyle fcmstyle = new FCMStyle.Builder().type(FCMStyleTypes.BIGTEXT_NOTIFICATION).text(bigTextNotification)
		 		.title(notificationTitle)
		 		.url(notificationURL)
		 		.lines(new String[] { notificationTitle, notificationSubTitle, bigTextNotification }).build();
		 	FCMLights fcmlights = new FCMLights.Builder().ledArgb(FCMLED.GREEN).ledOffMs(1).ledOnMs(1).build();
		 	FCM fcm = new FCM.Builder().collapseKey("ping").interactiveCategory("Accept")
		 		.delayWhileIdle(true).payload(new JSONObject().put("alert" , notificationMessage))
		 		.priority(FCMPriority.MAX)
		 		.visibility(Visibility.PUBLIC).sync(true).style(fcmstyle).lights(fcmlights).build();
		 	return fcm;
	}
	
	private class NotificationListner implements PushNotificationsResponseListener {
		private int statusCode;
		private String responseBody;
		
		public void onSuccess(int statusCode, String responseBody) {
			String response = "Successfully sent push notification! Status code: " + statusCode
				+ " Response body: " + responseBody;
			logger.debug("IBM Notification", response);
			setStatusCode(statusCode);
			setResponseBody(responseBody);
		}

		public void onFailure(Integer statusCode, String responseBody, Throwable t) {
			String response = "Failed sent push notification. Status code: " + statusCode + " Response body: "
					+ responseBody;
			logger.error("IBM Notification", response);
			setStatusCode(statusCode);
			setResponseBody(responseBody);
			if (t != null) {
				t.printStackTrace();
			}
		}
		public int getStatusCode() {
			return statusCode;
		}
		public void setStatusCode(int statusCode) {
			this.statusCode = statusCode;
		}
		public String getResponseBody() {
			return responseBody;
		}
		public void setResponseBody(String responseBody) {
			this.responseBody = responseBody;
		}
		
		
	}

}
