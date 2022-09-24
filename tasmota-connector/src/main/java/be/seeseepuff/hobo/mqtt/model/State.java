package be.seeseepuff.hobo.mqtt.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class State
{
	@JsonProperty("UptimeSec")
	private long uptime;
	@JsonProperty("Channel")
	private List<Integer> channels;

	public int getRed()
	{
		return getChannel(0);
	}

	public int getBlue()
	{
		return getChannel(1);
	}

	public int getGreen()
	{
		return getChannel(2);
	}

	public int getWhite()
	{
		return getChannel(3);
	}

	public int getChannel(int channel)
	{
		if (channel >= 0 && channel < channels.size())
			return channels.get(channel);
		else
			return 0;
	}
}

// Example message:
//{
//	"Time" : "2022-09-24T08:58:09",
//	"Uptime" : "0T00:09:13",
//	"UptimeSec" : 553,
//	"Heap" : 27,
//	"SleepMode" : "Dynamic",
//	"Sleep" : 10,
//	"LoadAvg" : 99,
//	"MqttCount" : 1,
//	"POWER" : "ON",
//	"Dimmer" : 100,
//	"Color" : "000000FF",
//	"HSBColor" : "0,0,0",
//	"White" : 100,
//	"Channel" : [ 0, 0, 0, 100 ],
//	"Scheme" : 0,
//	"Fade" : "ON",
//	"Speed" : 1,
//	"LedTable" : "ON",
//	"Wifi" : {
//	"AP" : 2,
//	"SSId" : "Nargles are behind it",
//	"BSSId" : "68:02:B8:EA:06:4C",
//	"Channel" : 1,
//	"Mode" : "11n",
//	"RSSI" : 100,
//	"Signal" : -49,
//	"LinkCount" : 1,
//	"Downtime" : "0T00:00:07"
//	}
//	}
