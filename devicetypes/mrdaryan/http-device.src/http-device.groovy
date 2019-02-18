/*
*  HTTP Button
*  Category: Device Handler
*/

import groovy.json.JsonSlurper

metadata {
	definition (name: "HTTP Device", namespace: "mrdaryan", author: "SC") {
	capability "Actuator"
    capability "Switch"
	capability "Momentary"
	capability "Sensor"
    //capability "Configuration"
     
    command "pushwithvar", ["string","string"]
  	}

	preferences {
		input("DeviceIP", "string", title:"Device IP Address", description: "Please enter your device's IP Address", required: true, displayDuringSetup: true)
		input("DevicePort", "string", title:"Device Port", description: "Empty assumes port 80.", required: false, displayDuringSetup: true)
		input("DevicePath", "string", title:"URL Path", description: "Rest of the URL, include forward slash.", defaultValue: "jsonrpc", displayDuringSetup: true)
		input("DeviceBody", "string", title:"Body", description: "Body of message, escape talking marks.", displayDuringSetup: true)
        input("DeviceContent", "string", title:"Content Type", description: "HTTP Content type.", defaultValue: "application/json", displayDuringSetup: true)
		input(name: "DevicePostGet", type: "enum", title: "POST or GET", options: ["POST","GET"], defaultValue: "POST", required: false, displayDuringSetup: true)
		section() {
			input("HTTPAuth", "bool", title:"Requires User Auth?", description: "Choose if the HTTP requires basic authentication", defaultValue: false, required: true, displayDuringSetup: true)
			input("HTTPUser", "string", title:"HTTP User", description: "Enter your basic username", required: false, displayDuringSetup: true)
			input("HTTPPassword", "string", title:"HTTP Password", description: "Enter your basic password", required: false, displayDuringSetup: true)
		}
	}

	// simulator metadata
	simulator {
	}

	// UI tile definitions
	tiles {
		standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
			state "off", label: 'Push', action: "momentary.push", backgroundColor: "#ffffff", nextState: "on"
			state "on", label: 'Push', action: "momentary.push", backgroundColor: "#53a7c0"
		}
		main "switch"
		details "switch"
	}
}

def parse(String description) {
	log.debug(description)
}

def on() {
	push()
}

def off() {
	push()
}

def push() {
	log.debug "---Sending command--- ${DevicePath}${BodyValue}"
    	sendEvent(name: "switch", value: "on", isStateChange: true, display: false)
    	sendEvent(name: "switch", value: "off", isStateChange: true, display: false)
    	sendEvent(name: "momentary", value: "pushed", isStateChange: true)
	runCmd(DevicePath,DeviceBody)
}

def pushwithvar(PathValue, BodyValue) {
	log.debug "---Sending command--- ${PathValue}${BodyValue}"
    	sendEvent(name: "switch", value: "on", isStateChange: true, display: false)
    	sendEvent(name: "switch", value: "off", isStateChange: true, display: false)
    	sendEvent(name: "momentary", value: "pushed", isStateChange: true)
	runCmd(PathValue,BodyValue)
}

def runCmd(String varCommand,varBody) {
	def host = DeviceIP
	def LocalDevicePort = ''
	if (DevicePort==null) { LocalDevicePort = "80" } else { LocalDevicePort = DevicePort }

	def userpassascii = "${HTTPUser}:${HTTPPassword}"
	def userpass = "Basic " + userpassascii.encodeAsBase64().toString()

	def path = varCommand
	def body = varBody
	//log.debug "Uses which method: $DevicePostGet"
	//log.debug "body is: $body"
    log.debug "The device id configured is: $device.deviceNetworkId"
	log.debug "path is: $path"
    log.debug "body is: $body"

	def headers = [:] 
	headers.put("HOST", "$host:$LocalDevicePort")
	headers.put("Content-Type", "${DeviceContent}")
	if (HTTPAuth) {
		headers.put("Authorization", userpass)
	}
	log.debug "The Header is $headers"
	def method = "POST"
	try {
		if (DevicePostGet.toUpperCase() == "GET") {
			method = "GET"
			}
		}
	catch (Exception e) {
		settings.DevicePostGet = "POST"
		log.debug e
		log.debug "You must not have set the preference for the DevicePOSTGET option"
	}
	log.debug "The method is $method"
	try {
		def hubAction = new physicalgraph.device.HubAction(
			method: method,
			path: path,
			body: body,
			headers: headers
			)
		log.debug hubAction
		return hubAction
	}
	catch (Exception e) {
		log.debug "Hit Exception $e on $hubAction"
	}
    
    //sendEvent
    if (varCommand == "off"){
    	sendEvent(name: "switch", value: "off")
        log.debug "Executing OFF"
    } else {
    	sendEvent(name: "switch", value: "on")
        log.debug "Executing ON"
    }
    
}