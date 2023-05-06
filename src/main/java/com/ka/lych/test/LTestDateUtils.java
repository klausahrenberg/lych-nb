package com.ka.lych.test;

import com.ka.lych.util.LDateUtils;
import com.ka.lych.util.LLog;

/**
 *
 * @author klausahrenberg
 */
public class LTestDateUtils {
    
    public static void main(String[] args) {
        
        //"Wed, 9 Jan 2019 07:36:39 KST"
        LLog.test(LTestDateUtils.class, "" + LDateUtils.rfc2822DateToDatetime("Tue, 4 Dec 2018 17:37:31 +0100"));
        LLog.test(LTestDateUtils.class, "" + LDateUtils.rfc2822DateToDatetime("Tue, 4 Dec 2018 17:37:31 CET"));
        LLog.test(LTestDateUtils.class, "" + LDateUtils.rfc2822DateToDatetime("Tue, 4 Dec 2018 17:37:31 +0100 (CET)"));
        LLog.test(LTestDateUtils.class, "" + LDateUtils.rfc2822DateToDatetime("Tue, 4 Dec 2018 17:37:31 (CET)"));
        LLog.test(LTestDateUtils.class, "" + LDateUtils.rfc2822DateToDatetime("Wed, 16 Jan 2019 07:36:39 KST"));
        LLog.test(LTestDateUtils.class, "" + LDateUtils.rfc2822DateToDatetime("Wed, 9 Jan 2019 07:36:39 KST"));
        LLog.test(LTestDateUtils.class, "" + LDateUtils.rfc2822DateToDatetime("Wed, 16 Jan 2019 07:36:39 UCT"));
        LLog.test(LTestDateUtils.class, "" + LDateUtils.rfc2822DateToDatetime("Wed, 16 Jan 2019 07:36:39 Z"));
        LLog.test(LTestDateUtils.class, "" + LDateUtils.rfc2822DateToDatetime("Mon, 2 Jul 2018 22:34:43 +0200 (CEST)"));
        LLog.test(LTestDateUtils.class, "" + LDateUtils.rfc2822DateToDatetime("Mon, 2 Jul 2018 22:34:43 CEST"));
        LLog.test(LTestDateUtils.class, "" + LDateUtils.rfc2822DateToDatetime("17 Nov 2018 04:24:19 +0100"));
        LLog.test(LTestDateUtils.class, "" + LDateUtils.rfc2822DateToDatetime("16 Dec 18 13:37:14 +0100"));
        LLog.test(LTestDateUtils.class, "" + LDateUtils.rfc2822DateToDatetime("Mon, 14 Jan 19 13:37:14 KST"));
        LLog.test(LTestDateUtils.class, "" + LDateUtils.rfc2822DateToDatetime("mon, 29 Oct 2018 10:08:54 +0200 (CEST)"));
        LLog.test(LTestDateUtils.class, "" + LDateUtils.rfc2822DateToDatetime("Tue, 06 Nov 2018 21:33:48 +0000 (UTC) (UTC)"));
    }
    
}
