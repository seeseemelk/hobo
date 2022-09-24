package be.seeseepuff.hobo.mqtt.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class Discovery
{
	private String ip;
	@JsonProperty("dn")
	private String deviceName;
	@JsonProperty("fn")
	private List<String> friendlyNames;
	@JsonProperty("hn")
	private String hostName;
	private String mac;
	@JsonProperty("md")
	private String module;
	@JsonProperty("sw")
	private String firmwareVersion;
	@JsonProperty("t")
	private String topic;
	@JsonProperty("ver")
	private int protocolVersion;
	@JsonProperty("ofln")
	private String offlinePayload;
	@JsonProperty("onln")
	private String onlinePayload;
}
