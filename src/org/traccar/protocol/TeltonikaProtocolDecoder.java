/*
 * Copyright 2013 - 2017 Anton Tananaev (anton@traccar.org)
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
package org.traccar.protocol;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.socket.DatagramChannel;
import org.traccar.BaseProtocolDecoder;
import org.traccar.Context;
import org.traccar.DeviceSession;
import org.traccar.database.DeviceManager;
import org.traccar.helper.BitUtil;
import org.traccar.helper.UnitsConverter;
import org.traccar.model.CellTower;
import org.traccar.model.Device;
import org.traccar.model.Network;
import org.traccar.model.Position;

import javax.json.JsonObjectBuilder;
import javax.json.Json;
import javax.json.JsonObject;

import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.LinkedList;
import java.util.Date;
import java.util.Map;

public class TeltonikaProtocolDecoder extends BaseProtocolDecoder {

    public TeltonikaProtocolDecoder(TeltonikaProtocol protocol) {
        super(protocol);
    }

    private DeviceSession parseIdentification(Channel channel, SocketAddress remoteAddress, ChannelBuffer buf) {

        int length = buf.readUnsignedShort();
        String imei = buf.toString(buf.readerIndex(), length, StandardCharsets.US_ASCII);
        DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, imei);

        if (channel != null) {
            ChannelBuffer response = ChannelBuffers.directBuffer(1);
            if (deviceSession != null) {
                response.writeByte(1);
            } else {
                response.writeByte(0);
            }
            channel.write(response);
        }
        return deviceSession;
    }

    public static final int CODEC_GH3000 = 0x07;
    public static final int CODEC_FM4X00 = 0x08;
    public static final int CODEC_12 = 0x0C;

    public static final String PREAMBLE_1 = "&";
    public static final String PREAMBLE_2 = "";
    public static final String PREAMBLE_3 = "";
    public static final String PREAMBLE = PREAMBLE_1.concat(PREAMBLE_2).concat(PREAMBLE_3);
    public static final String OID_REGEX = "^".concat(PREAMBLE).concat("OID=.*$");
    public static final String TKT_REGEX = "^".concat(PREAMBLE).concat("TKT=.*$");
    public static final String ACK_REGEX = "^".concat(PREAMBLE).concat("ACK=.*$");
    public static final String STA_REGEX = "^".concat(PREAMBLE).concat("STA=.*$");
    public static final String TKT_ACK = PREAMBLE.concat("TKT ACK");
    public static final String DO_DUMP = PREAMBLE.concat("DO DUMP");
    public static final String SET_TMS = PREAMBLE.concat("SET TMS");
    public static final String SET_DRI = PREAMBLE.concat("SET DRI");
    public static final String SET_VEH = PREAMBLE.concat("SET VEH");
    public static final String SET_LIN = PREAMBLE.concat("SET LIN");
    public static final String SET_ATTR = PREAMBLE.concat("SETATTR");
    public static final String VENABLE = PREAMBLE.concat("VENABLE");
    public static final String VDISABL = PREAMBLE.concat("VDISABL");
    public static final String VREBOOT = PREAMBLE.concat("VREBOOT");
    public static final String GETSTAT = PREAMBLE.concat("GETSTAT");
    public static final String TICKETS_TERMINATOR = "\r\n";
    public static final Integer MAX_SEND_RETRY = 1;

    private void decodeSerial(Position position, ChannelBuffer buf, Channel channel) {

        getLastLocation(position, null);

        position.set(Position.KEY_TYPE, buf.readUnsignedByte());

        String data = buf.readBytes(buf.readInt()).toString(StandardCharsets.US_ASCII);

        DeviceManager deviceManager = Context.getDeviceManager();
        Device device = deviceManager.getDeviceById(position.getDeviceId());

        if (data.matches(OID_REGEX)) {
            position.set("oid", data.substring(PREAMBLE.length() + 4));
        // no ACK for OID
        } else if (data.split(TICKETS_TERMINATOR)[0].matches(TKT_REGEX)) {
            position.set("tkt_list", data.substring(PREAMBLE.length()
                    + 4, data.length() - TICKETS_TERMINATOR.length() - 4));
            position.set("tkt_terminator", TICKETS_TERMINATOR);
            if (channel != null) {
                String crc = data.substring(data.lastIndexOf(TICKETS_TERMINATOR) + TICKETS_TERMINATOR.length());
                String ack = TKT_ACK.concat(crc);
                ChannelBuffer response = TeltonikaProtocolEncoder.encodeString(ack);
                channel.write(response);
            }
        } else if (data.split(TICKETS_TERMINATOR)[0].matches(ACK_REGEX)) {
            String ackType = data.substring(PREAMBLE.length() + 4, data.length()).split(TICKETS_TERMINATOR)[0];
            switch (ackType) {
                case "TMS":
                    device.set("set_timestamp", 0);
                    break;
                case "DRI":
                    device.set("set_driver", 0);
                    break;
                case "VEH":
                    device.set("set_vehicle", 0);
                    break;
                case "LIN":
                    device.set("set_line", 0);
                    break;
                case "VREBOOT":
                    device.set("reboot", 0);
                    break;
                case "VENABLE":
                    device.set("set_enable", 0);
                    break;
                case "VDISABLE":
                    device.set("set_disable", 0);
                    break;
                case "SETATTR":
                    device.set("set_attributes", 0);
                    break;
                default:
                    break;
            }
        } else if (data.split(TICKETS_TERMINATOR)[0].matches(STA_REGEX)) {
            // todo save validators status
            device.set("get_status", 0);
        } else {
            position.set("command", data);
        }
    }

    private void decodeParameter(Position position, int id, ChannelBuffer buf, int length) {
        switch (id) {
            case 1:
            case 2:
            case 3:
            case 4:
                position.set("di" + id, buf.readUnsignedByte());
                break;
            case 9:
                position.set(Position.PREFIX_ADC + 1, buf.readUnsignedShort());
                break;
            case 66:
                position.set(Position.KEY_POWER, buf.readUnsignedShort() + "mV");
                break;
            case 67:
                position.set(Position.KEY_BATTERY, buf.readUnsignedShort() + "mV");
                break;
            case 70:
                position.set("pcbTemp", (length == 4 ? buf.readInt() : buf.readShort()) * 0.1);
                break;
            case 72:
                position.set(Position.PREFIX_TEMP + 1, buf.readInt() * 0.1);
                break;
            case 73:
                position.set(Position.PREFIX_TEMP + 2, buf.readInt() * 0.1);
                break;
            case 74:
                position.set(Position.PREFIX_TEMP + 3, buf.readInt() * 0.1);
                break;
            case 78:
                byte[] rfidBytes = new byte[8];
                buf.readBytes(rfidBytes, 0, 8);
                Long rfid = parseRfid(rfidBytes);
//                position.set(Position.KEY_RFID, buf.readLong());
                position.set(Position.KEY_RFID, rfid);
                break;
            case 182:
                position.set(Position.KEY_HDOP, buf.readUnsignedShort() * 0.1);
                break;
            default:
                switch (length) {
                    case 1:
                        position.set(Position.PREFIX_IO + id, buf.readUnsignedByte());
                        break;
                    case 2:
                        position.set(Position.PREFIX_IO + id, buf.readUnsignedShort());
                        break;
                    case 4:
                        position.set(Position.PREFIX_IO + id, buf.readUnsignedInt());
                        break;
                    case 8:
                    default:
                        position.set(Position.PREFIX_IO + id, buf.readLong());
                        break;
                }
                break;
        }
    }

    private void decodeLocation(Position position, ChannelBuffer buf, int codec) {

        int globalMask = 0x0f;

        if (codec == CODEC_GH3000) {

            long time = buf.readUnsignedInt() & 0x3fffffff;
            time += 1167609600; // 2007-01-01 00:00:00

            globalMask = buf.readUnsignedByte();
            if (BitUtil.check(globalMask, 0)) {

                position.setTime(new Date(time * 1000));

                int locationMask = buf.readUnsignedByte();

                if (BitUtil.check(locationMask, 0)) {
                    position.setLatitude(buf.readFloat());
                    position.setLongitude(buf.readFloat());
                }

                if (BitUtil.check(locationMask, 1)) {
                    position.setAltitude(buf.readUnsignedShort());
                }

                if (BitUtil.check(locationMask, 2)) {
                    position.setCourse(buf.readUnsignedByte() * 360.0 / 256);
                }

                if (BitUtil.check(locationMask, 3)) {
                    position.setSpeed(UnitsConverter.knotsFromKph(buf.readUnsignedByte()));
                }

                if (BitUtil.check(locationMask, 4)) {
                    int satellites = buf.readUnsignedByte();
                    position.set(Position.KEY_SATELLITES, satellites);
                    position.setValid(satellites >= 3);
                }

                if (BitUtil.check(locationMask, 5)) {
                    position.setNetwork(new Network(
                            CellTower.fromLacCid(buf.readUnsignedShort(), buf.readUnsignedShort())));
                }

                if (BitUtil.check(locationMask, 6)) {
                    buf.readUnsignedByte(); // rssi
                }

                if (BitUtil.check(locationMask, 7)) {
                    position.set("operator", buf.readUnsignedInt());
                }

            } else {

                getLastLocation(position, new Date(time * 1000));

            }

        } else {

            position.setTime(new Date(buf.readLong()));

            position.set("priority", buf.readUnsignedByte());

            position.setLongitude(buf.readInt() / 10000000.0);
            position.setLatitude(buf.readInt() / 10000000.0);
            position.setAltitude(buf.readShort());
            position.setCourse(buf.readUnsignedShort());

            int satellites = buf.readUnsignedByte();
            position.set(Position.KEY_SATELLITES, satellites);

            position.setValid(satellites != 0);

            position.setSpeed(UnitsConverter.knotsFromKph(buf.readUnsignedShort()));

            position.set(Position.KEY_EVENT, buf.readUnsignedByte());

            buf.readUnsignedByte(); // total IO data records

        }

        // Read 1 byte data
        if (BitUtil.check(globalMask, 1)) {
            int cnt = buf.readUnsignedByte();
            for (int j = 0; j < cnt; j++) {
                decodeParameter(position, buf.readUnsignedByte(), buf, 1);
            }
        }

        // Read 2 byte data
        if (BitUtil.check(globalMask, 2)) {
            int cnt = buf.readUnsignedByte();
            for (int j = 0; j < cnt; j++) {
                decodeParameter(position, buf.readUnsignedByte(), buf, 2);
            }
        }

        // Read 4 byte data
        if (BitUtil.check(globalMask, 3)) {
            int cnt = buf.readUnsignedByte();
            for (int j = 0; j < cnt; j++) {
                decodeParameter(position, buf.readUnsignedByte(), buf, 4);
            }
        }

        // Read 8 byte data
        if (codec == CODEC_FM4X00) {
            int cnt = buf.readUnsignedByte();
            for (int j = 0; j < cnt; j++) {
                decodeParameter(position, buf.readUnsignedByte(), buf, 8);
            }
        }

    }

    private List<Position> parseData(
            Channel channel, SocketAddress remoteAddress, ChannelBuffer buf, int packetId, String... imei) {
        List<Position> positions = new LinkedList<>();

        if (!(channel instanceof DatagramChannel)) {
            buf.readUnsignedInt(); // data length
        }

        int codec = buf.readUnsignedByte();
        int count = buf.readUnsignedByte();

        DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, imei);

        if (deviceSession == null) {
            return null;
        }

        for (int i = 0; i < count; i++) {
            Position position = new Position();
            position.setProtocol(getProtocolName());

            position.setDeviceId(deviceSession.getDeviceId());

            if (codec == CODEC_12) {
                decodeSerial(position, buf, channel);
            } else {
                decodeLocation(position, buf, codec);
            }

            positions.add(position);
        }

        if (channel != null) {
            if (channel instanceof DatagramChannel) {
                ChannelBuffer response = ChannelBuffers.directBuffer(5);
                response.writeShort(3);
                response.writeShort(packetId);
                response.writeByte(0x02);
                channel.write(response, remoteAddress);
            } else if (codec != CODEC_12) {
                ChannelBuffer response = ChannelBuffers.directBuffer(4);
                response.writeInt(count);
                channel.write(response);

//              Send commands according to device status
                DeviceManager deviceManager = Context.getDeviceManager();
                Device device = deviceManager.getDeviceById(deviceSession.getDeviceId());
                processStatus(device, channel);

            }
        }

        return positions;
    }

    private void processStatus(Device device, Channel channel) {
        Map attributes = device.getAttributes();

        if (device.getInteger("dump") > 0 && device.getInteger("dump") <= MAX_SEND_RETRY) {
            ChannelBuffer dumpCommand = TeltonikaProtocolEncoder.encodeString(DO_DUMP);
            channel.write(dumpCommand);
            device.set("dump", device.getInteger("dump") + 1);
        }
        if (device.getInteger("set_timestamp") > 0
                && device.getInteger("set_timestamp") <= MAX_SEND_RETRY) {
            Date now = new Date();
            long timestamp = now.getTime();

            JsonObjectBuilder builder = Json.createObjectBuilder().
                    add("time", timestamp);
            JsonObject payload = builder.build();

            String command = SET_TMS.concat(" ").concat(payload.toString());
            ChannelBuffer dumpCommand = TeltonikaProtocolEncoder.encodeString(command);
            channel.write(dumpCommand);
            device.set("set_timestamp", device.getInteger("set_timestamp") + 1);
        }
        if (device.getInteger("set_attributes") > 0
                && device.getInteger("set_attributes") <= MAX_SEND_RETRY) {
            Date now = new Date();
            long timestamp = now.getTime();

            JsonObjectBuilder builder = Json.createObjectBuilder();

            builder.add("time", Long.toString(timestamp));
            if (attributes.containsKey("driver_name")) {
                builder.add("dri_name", (String) attributes.get("driver_name"));
            }
            if (attributes.containsKey("driver_id")) {
                builder.add("dri_id", (Integer) attributes.get("driver_id"));
            }
            if (attributes.containsKey("vehicle_name")) {
                builder.add("veh_name", (String) attributes.get("vehicle_name"));
            }
            if (attributes.containsKey("vehicle_id")) {
                builder.add("veh_id", (Integer) attributes.get("vehicle_id"));
            }
            if (attributes.containsKey("line_name")) {
                builder.add("lin_name", (String) attributes.get("line_name"));
            }
            if (attributes.containsKey("line_id")) {
                builder.add("lin_id", (Integer) attributes.get("line_id"));
            }

            JsonObject payload = builder.build();
            String command = SET_ATTR.concat(" ").concat(payload.toString());
            ChannelBuffer dumpCommand = TeltonikaProtocolEncoder.encodeString(command);
            channel.write(dumpCommand);
            device.set("set_attributes", device.getInteger("set_attributes") + 1);
        }
        if (device.getInteger("set_driver") > 0
                && device.getInteger("set_driver") <= MAX_SEND_RETRY) {

            JsonObjectBuilder builder = Json.createObjectBuilder();

            if (attributes.containsKey("driver_name")) {
                builder.add("name", (String) attributes.get("driver_name"));
            }
            if (attributes.containsKey("driver_id")) {
                builder.add("id", (Integer) attributes.get("driver_id"));

                JsonObject payload = builder.build();
                String command = SET_DRI.concat(" ").concat(payload.toString());
                ChannelBuffer dumpCommand = TeltonikaProtocolEncoder.encodeString(command);
                channel.write(dumpCommand);
                device.set("set_driver", device.getInteger("set_driver") + 1);
            }
        }
        if (device.getInteger("set_vehicle") > 0
                && device.getInteger("set_vehicle") <= MAX_SEND_RETRY) {
            JsonObjectBuilder builder = Json.createObjectBuilder();

            if (attributes.containsKey("vehicle_name")) {
                builder.add("name", (String) attributes.get("vehicle_name"));
            }
            if (attributes.containsKey("vehicle_id")) {
                builder.add("id", (Integer) attributes.get("vehicle_id"));

                JsonObject payload = builder.build();
                String command = SET_VEH.concat(" ").concat(payload.toString());
                ChannelBuffer dumpCommand = TeltonikaProtocolEncoder.encodeString(command);
                channel.write(dumpCommand);
                device.set("set_vehicle", device.getInteger("set_vehicle") + 1);
            }
        }
        if (device.getInteger("set_line") > 0
                && device.getInteger("set_line") <= MAX_SEND_RETRY) {
            JsonObjectBuilder builder = Json.createObjectBuilder();

            if (attributes.containsKey("line_name")) {
                builder.add("name", (String) attributes.get("line_name"));
            }
            if (attributes.containsKey("line_id")) {
                builder.add("id", (Integer) attributes.get("line_id"));

                JsonObject payload = builder.build();
                String command = SET_LIN.concat(" ").concat(payload.toString());
                ChannelBuffer dumpCommand = TeltonikaProtocolEncoder.encodeString(command);
                channel.write(dumpCommand);
                device.set("set_line", device.getInteger("set_line") + 1);
            }
        }
        if (device.getInteger("get_status") > 0
                && device.getInteger("get_status") <= MAX_SEND_RETRY) {
            ChannelBuffer dumpCommand = TeltonikaProtocolEncoder.encodeString(GETSTAT);
            channel.write(dumpCommand);
            device.set("get_status", device.getInteger("get_status") + 1);
        }
        if (device.getInteger("set_enable") > 0
                && device.getInteger("set_enable") <= MAX_SEND_RETRY) {
            ChannelBuffer dumpCommand = TeltonikaProtocolEncoder.encodeString(VENABLE);
            channel.write(dumpCommand);
            device.set("set_enable", device.getInteger("set_enable") + 1);
        }
        if (device.getInteger("set_disable") > 0
                && device.getInteger("set_disable") <= MAX_SEND_RETRY) {
            ChannelBuffer dumpCommand = TeltonikaProtocolEncoder.encodeString(VDISABL);
            channel.write(dumpCommand);
            device.set("set_disable", device.getInteger("set_disable") + 1);
        }
        if (device.getInteger("reboot") > 0
                && device.getInteger("reboot") <= MAX_SEND_RETRY) {
            ChannelBuffer dumpCommand = TeltonikaProtocolEncoder.encodeString(VREBOOT);
            channel.write(dumpCommand);
            device.set("reboot", device.getInteger("reboot") + 1);
        }
    }

    @Override
    protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {

        ChannelBuffer buf = (ChannelBuffer) msg;

        if (channel instanceof DatagramChannel) {
            return decodeUdp(channel, remoteAddress, buf);
        } else {
            return decodeTcp(channel, remoteAddress, buf);
        }
    }

    private Object decodeTcp(Channel channel, SocketAddress remoteAddress, ChannelBuffer buf) throws Exception {

        if (buf.getUnsignedShort(0) > 0) {
            parseIdentification(channel, remoteAddress, buf);
        } else {
            buf.skipBytes(4);
            return parseData(channel, remoteAddress, buf, 0);
        }

        return null;
    }

    private Object decodeUdp(Channel channel, SocketAddress remoteAddress, ChannelBuffer buf) throws Exception {

        buf.skipBytes(2);
        int packetId = buf.readUnsignedShort();
        buf.skipBytes(2);
        String imei = buf.readBytes(buf.readUnsignedShort()).toString(StandardCharsets.US_ASCII);

        return parseData(channel, remoteAddress, buf, packetId, imei);

    }

    private Long parseRfid(byte[] rfidBytes) {

        ByteBuffer buffer = ByteBuffer.allocate(8);
        for (int i = 0; i < 4; i++) {
            buffer.put((byte) 0x00);
        }
        for (int i = 0; i < 4; i++) {
            buffer.put(rfidBytes[i + 3]);
        }

        buffer.flip();  //need flip
        return buffer.getLong();

    }

}
