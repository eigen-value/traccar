/*
 * Copyright 2017 Lucio Rossi (l.rossi@unina.it)
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

package org.traccar.model;

import java.util.Date;

public class DriverStamping extends Message{

    private String imei;

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    private String cardSerial;

    public String getCardSerial() {
        return cardSerial;
    }

    public void setCardSerial(String imei) {
        this.cardSerial = imei;
    }

    private Date stmpTs;

    public Date getStmpTs() {
        if (stmpTs != null) {
            return new Date(stmpTs.getTime());
        } else {
            return null;
        }
    }

    public void setStmpTs(Date stmpTs) {
        if (stmpTs != null) {
            this.stmpTs = new Date(stmpTs.getTime());
        } else {
            this.stmpTs = null;
        }
    }

    private Date serverTime;

    public Date getServerTime() {
        if (serverTime != null) {
            return new Date(serverTime.getTime());
        } else {
            return null;
        }
    }

    public void setServerTime(Date serverTime) {
        if (serverTime != null) {
            this.serverTime = new Date(serverTime.getTime());
        } else {
            this.serverTime = null;
        }
    }

}
