/*
 * Copyright (c) 2016.
 *
 * DReflect and Minuku Libraries by Shriti Raj (shritir@umich.edu) and Neeraj Kumar(neerajk@uci.edu) is licensed under a Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License.
 * Based on a work at https://github.com/Shriti-UCI/Minuku-2.
 *
 *
 * You are free to (only if you meet the terms mentioned below) :
 *
 * Share — copy and redistribute the material in any medium or format
 * Adapt — remix, transform, and build upon the material
 *
 * The licensor cannot revoke these freedoms as long as you follow the license terms.
 *
 * Under the following terms:
 *
 * Attribution — You must give appropriate credit, provide a link to the license, and indicate if changes were made. You may do so in any reasonable manner, but not in any way that suggests the licensor endorses you or your use.
 * NonCommercial — You may not use the material for commercial purposes.
 * ShareAlike — If you remix, transform, or build upon the material, you must distribute your contributions under the same license as the original.
 * No additional restrictions — You may not apply legal terms or technological measures that legally restrict others from doing anything the license permits.
 */

package edu.umich.si.inteco.minuku.config;

import edu.umich.si.inteco.minuku.BuildConfig;
import edu.umich.si.inteco.minuku.logger.Log;

/**
 * Created by shriti on 7/17/16.
 */
public class Constants {
    public static final String YES = "YES";
    public static final String NO = "NO";

    private boolean firebaseUrlOverridenOnce = false;
    private static Constants instance;

    public void setFirebaseUrl(String firebaseUrl) throws IllegalStateException {
        if (firebaseUrlOverridenOnce) {
            throw new IllegalStateException("Cannot set firebase url more than once");
        }
        FIREBASE_URL = firebaseUrl;
        firebaseUrlOverridenOnce = true;
    }

    private Constants() {

    }

    public static Constants getInstance() {
        if (instance == null) {
            instance = new Constants();
        }
        return instance;
    }

    public String getFirebaseUrl() {
        return FIREBASE_URL;
    }

    public String getFirebaseUrlForUsers() {
        return FIREBASE_URL + "/users";
    }

    public String getFirebaseUrlForMoods() {
        return FIREBASE_URL + "/moods";
    }

    public String getFirebaseUrlForNotes() {
        return FIREBASE_URL + "/notes";
    }

    public String getFirebaseUrlForNotifications() {
        return FIREBASE_URL + "/notifications";
    }

    public String getFirebaseUrlForImages() {
        return FIREBASE_URL + "/images";
    }

    public String getFirebaseUrlForLocation() {
        return FIREBASE_URL + "/location";
    }

    public String getFirebaseUrlForSemanticLocation() {
        return FIREBASE_URL + "/semantic_location";
    }

    public String getFirebaseUrlForQuestions() {
        return FIREBASE_URL + "/questions";
    }

    public String getFirebaseUrlForMCQ() {
        return getFirebaseUrlForQuestions() + "/mcq";
    }

    public String getFirebaseUrlForFreeResponse() {
        return getFirebaseUrlForQuestions() + "/freeresponse";
    }

    public String getFirebaseUrlForUserSubmissionStats() {
        return FIREBASE_URL + "/submissionstats";
    }

    public String getFirebaseUrlForDiabetesLog() {
        return FIREBASE_URL + "/diabetes_log";
    }

    public String getFirebaseUrlForEODQuestionAnswer() {
        return FIREBASE_URL + "/EOD_question_answer";
    }

    public String getFirebaseUrlForTag() {
        return FIREBASE_URL + "/tags";
    }

    public String getFirebaseUrlForRecentTags() {
        return FIREBASE_URL + "/recent_tags";
    }

    public String getFirebaseUrlForTimeinePatch() {
        return FIREBASE_URL + "/eod_timeline_notes";
    }

    public String getFirebaseUrlForMissedReportPrompt() {
        return FIREBASE_URL + "/missed_report_prompt_QnA";
    }

    public String getFirebaseUrlForDiaryScreenshot() {
        return FIREBASE_URL + "/diary_screenshot";
    }

    // Firebase config
    private String FIREBASE_URL = BuildConfig.UNIQUE_FIREBASE_ROOT_URL;


    // Provider stuff
    public static final String GOOGLE_AUTH_PROVIDER = "google";
    public static final String PASSWORD_PROVIDER = "password";
    //public static final String PROVIDER_DATA_DISPLAY_NAME = "displayName";

    // Google provider hashkeys
    public static final String GGL_PROVIDER_USERNAME_KEY = "username";
    public static final String GGL_PROVIDER_EMAIL_KEY = "email";

    // Shared pref ids
    public static final String ID_SHAREDPREF_EMAIL = "email";
    public static final String ID_SHAREDPREF_PROVIDER = "provider";
    //public static final String ID_SHAREDPREF_DISPLAYNAME = "displayName";

    public static final String KEY_SIGNUP_EMAIL = "SIGNUP_EMAIL";
    public static final String KEY_ENCODED_EMAIL = "ENCODED_EMAIL";

    public static final String LOG_ERROR = "Error:";


    // Prompt service related constants
    public static final int PROMPT_SERVICE_REPEAT_MILLISECONDS = 1000 * 60; // 1 minute
    //changing from 50 mins to 15 mins, users were getting it close to bedtime
    public static final int DIARY_NOTIFICATION_SERVICE_REPEAT_MILLISECONDS = 15 * 60 * 1000; //15 minutes


    // Notification related constants
    public static final String CAN_SHOW_NOTIFICATION = "ENABLE_NOTIFICATIONS";

    public static final String MOOD_REMINDER_TITLE = "How are you feeling right now?";
    public static final String MOOD_REMINDER_MESSAGE = "Tap here to report your mood.";

    public static final String MOOD_ANNOTATION_TITLE = "Tell us more about your mood";
    public static final String MOOD_ANNOTATION_MESSAGE = "Tap here answer a quick question.";

    public static final String MISSED_ACTIVITY_DATA_PROMPT_TITLE = "We want to hear from you!";
    public static final String MISSED_ACTIVITY_DATA_PROMPT_MESSAGE = "Tap here to answer some questions.";

    public static final String EOD_DIARY_PROMPT_TITLE = "Diary entry";
    public static final String EOD_DIARY_PROMPT_MESSAGE = "Tap here to complete today's diary.";




    //default queue size
    public static final int DEFAULT_QUEUE_SIZE = 20;

    //specific queue sizes
    public static final int LOCATION_QUEUE_SIZE = 50;
    public static final int IMAGE_QUEUE_SIZE = 20;
    public static final int MOOD_QUEUE_SIZE = 20;

    public static final int MOOD_STREAM_GENERATOR_UPDATE_FREQUENCY_MINUTES = 15;
    public static final int IMAGE_STREAM_GENERATOR_UPDATE_FREQUENCY_MINUTES = 30;
    public static final int FOOD_IMAGE_STREAM_GENERATOR_UPDATE_FREQUENCY_MINUTES = 180;

    public static final int MOOD_NOTIFICATION_EXPIRATION_TIME = 30 * 60 /* 30 minutes*/;
    //changing missed report notification expiry to 2 hours as users are missing
    public static final int MISSED_REPORT_NOTIFICATION_EXPIRATION_TIME =  2 * 60 * 60 /* 120 minutes*/;
    //changing diary notification expiry to 2 hours as users are missing it
    public static final int DIARY_NOTIFICATION_EXPIRATION_TIME = 2 * 60 * 60 /* 120 minutes*/;

    public static final String TAPPED_NOTIFICATION_ID_KEY = "TAPPED_NOTIFICATION_ID" ;
    public static final String SELECTED_LOCATIONS = "USERPREF_SELECTED_LOCATIONS";
    public static final String BUNDLE_KEY_FOR_QUESTIONNAIRE_ID = "QUESTIONNAIRE_ID";
    public static final String BUNDLE_KEY_FOR_NOTIFICATION_SOURCE = "NOTIFICATION_SOURCE";
    public static final String APP_NAME = "DReflect";
    public static final String RUNNING_APP_DECLARATION = APP_NAME + " is running in the background";
    public static final long INTERNAL_LOCATION_UPDATE_FREQUENCY = 1 * 300 * 1000;
    public static final float LOCATION_MINUMUM_DISPLACEMENT_UPDATE_THRESHOLD = 50 ;

    public static final String DIABETES_LOG_NOTIFICATION_SOURCE = "DIABETES_LOG";


}
