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

import org.traccar.helper.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TicketStamping extends Message {

    public static final String TICKET_FIELD_SEPARATOR = ",";
    public static final String TICKET_FIELD_SECONDARY_SEPARATOR = " ";
    public static final int MIN_TICKET_FIELDS = 16;
    public static final String MAG_REGEX = "^MAG.*?";
    public static final String CSC_REGEX = "^CSC.*?";

    public TicketStamping(String ticket) {

//      parse fields depending on tkt kind, with separator

        String[] fields;

        if (ticket.contains(TICKET_FIELD_SEPARATOR)) {
            fields = ticket.split(TICKET_FIELD_SEPARATOR);
        } else {
            fields = ticket.split(TICKET_FIELD_SECONDARY_SEPARATOR);
        }

        if (fields.length < MIN_TICKET_FIELDS) {
            Log.warning("Unable to parse ticket: ".concat(ticket));
            return;
        }

        String obtName = fields[3];
        this.setObtName(obtName);

        String ticketKind = fields[0].substring(0, 3);
        this.setTicketKind(ticketKind);

        int numRecord = Integer.parseInt(fields[0].substring(4));
        this.setNumRecord(numRecord);

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
        try {
            Date oblTs = dateFormat.parse(fields[1].concat(" ").concat(fields[2]));
            setOblTs(oblTs);
        } catch (ParseException s) {
            Log.warning(s);
        }

        String zone = fields[4];
        this.setZone(zone);

        String lineCode = fields[5];
        this.setLineCode(lineCode);

        String lineName = fields[6];
        this.setLineName(lineName);

        String vehicleCode = fields[7];
        this.setVehicleCode(vehicleCode);

        String vehicleName = fields[8];
        this.setVehicleName(vehicleName);

        String ring = fields[9];
        this.setRing(ring);

        String stopPointCode = fields[10];
        this.setStopPointCode(stopPointCode);

        String stopPointName = fields[11];
        this.setStopPointName(stopPointName);

        String driverCode = fields[12];
        this.setDriverCode(driverCode);

        String driverName = fields[13];
        this.setDriverName(driverName);

        long ticketSerial = Long.parseLong(fields[14]);
        this.setTicketSerial(ticketSerial);

        int ticketId = Integer.parseInt(fields[15]);
        this.setTicketId(ticketId);

        if (fields[0].matches(MAG_REGEX)) {

            String ticketType = fields[16];
            this.setTicketType(ticketType);

            int ticketDuration = Integer.parseInt(fields[17]);
            this.setTicketDuration(ticketDuration);

            String oblResult = fields[18].replaceFirst(";$", "");
            this.setOblResult(oblResult);


        } else if (fields[0].matches(CSC_REGEX)) {

            String ticketType = "";
            this.setTicketType(ticketType);

            int ticketDuration = 0;
            this.setTicketDuration(ticketDuration);

            String oblResult = fields[16].replaceFirst(";$", "");
            this.setOblResult(oblResult);

        }

    }

    private String obtName;

    public String getObtName() {
        return obtName;
    }

    public void setObtName(String obtName) {
        this.obtName = obtName;
    }

    private String ticketKind;

    public String getTicketKind() {
        return ticketKind;
    }

    public void setTicketKind(String ticketKind) {
        this.ticketKind = ticketKind;
    }

    private int numRecord;

    public int getNumRecord() {
        return numRecord;
    }

    public void setNumRecord(int numRecord) {
        this.numRecord = numRecord;
    }

    private Date oblTs;

    public Date getOblTs() {
        if (oblTs != null) {
            return new Date(oblTs.getTime());
        } else {
            return null;
        }
    }

    public void setOblTs(Date oblTs) {
        if (oblTs != null) {
            this.oblTs = new Date(oblTs.getTime());
        } else {
            this.oblTs = null;
        }
    }

    private String zone;

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    private String lineCode;

    public String getLineCode() {
        return lineCode;
    }

    public void setLineCode(String lineCode) {
        this.lineCode = lineCode;
    }

    private String lineName;

    public String getLineName() {
        return lineName;
    }

    public void setLineName(String lineName) {
        this.lineName = lineName;
    }

    private String vehicleCode;

    public String getVehicleCode() {
        return vehicleCode;
    }

    public void setVehicleCode(String vehicleCode) {
        this.vehicleCode = vehicleCode;
    }

    private String vehicleName;

    public String getVehicleName() {
        return vehicleName;
    }

    public void setVehicleName(String vehicleName) {
        this.vehicleName = vehicleName;
    }

    private String ring;

    public String getRing() {
        return ring;
    }

    public void setRing(String ring) {
        this.ring = ring;
    }

    private String stopPointCode;

    public String getStopPointCode() {
        return stopPointCode;
    }

    public void setStopPointCode(String stopPointCode) {
        this.stopPointCode = stopPointCode;
    }

    private String stopPointName;

    public String getStopPointName() {
        return stopPointName;
    }

    public void setStopPointName(String stopPointName) {
        this.stopPointName = stopPointName;
    }

    private String driverCode;

    public String getDriverCode() {
        return driverCode;
    }

    public void setDriverCode(String driverCode) {
        this.driverCode = driverCode;
    }

    private String driverName;

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    private long ticketSerial;

    public long getTicketSerial() {
        return ticketSerial;
    }

    public void setTicketSerial(long ticketSerial) {
        this.ticketSerial = ticketSerial;
    }

    private int ticketId;

    public int getTicketId() {
        return ticketId;
    }

    public void setTicketId(int ticketId) {
        this.ticketId = ticketId;
    }

    private String ticketType;

    public String getTicketType() {
        return ticketType;
    }

    public void setTicketType(String ticketType) {
        this.ticketType = ticketType;
    }

    private int ticketDuration;

    public int getTicketDuration() {
        return ticketDuration;
    }

    public void setTicketDuration(int ticketDuration) {
        this.ticketDuration = ticketDuration;
    }

    private String oblResult;

    public String getOblResult() {
        return oblResult;
    }

    public void setOblResult(String oblResult) {
        this.oblResult = oblResult;
    }

//    private Date dumpTs;
//
//    public Date getDumpTs() {
//        if (dumpTs != null) {
//            return new Date(dumpTs.getTime());
//        } else {
//            return null;
//        }
//    }
//
//    public void setDumpTs(Date dumpTs) {
//        if (dumpTs != null) {
//            this.dumpTs = new Date(dumpTs.getTime());
//        } else {
//            this.dumpTs = null;
//        }
//    }

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

//    private int obObtId;
//
//    public int getObObtId() {
//        return obObtId;
//    }
//
//    public void setObObtId(int obObtId) {
//        this.obObtId = obObtId;
//    }

}
