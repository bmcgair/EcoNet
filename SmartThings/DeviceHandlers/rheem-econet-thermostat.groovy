/**
 *  Rheem Econet Thermostat
 *
 * Contributors
 *
 * Contributors:
 * Justin Huff
 * fsrt
 * Brian Spranger
 * copyninja
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *  Last Updated : 2018-04-22
 *
 */
metadata {
	definition (name: "Rheem Econet Thermostat", namespace: "bmcgair", author: "Bill McGair") {
		capability "Actuator"
		capability "Thermostat"
		capability "Sensor"
		capability "Polling"
		capability "Refresh"
		capability "Relative Humidity Measurement"
		capability "Temperature Measurement"
		capability "Thermostat Cooling Setpoint"
		capability "Thermostat Fan Mode"
		capability "Thermostat Heating Setpoint"
		capability "Thermostat Mode"
		capability "Thermostat Operating State"
		capability "Thermostat Setpoint"
		
		command "heatLevelUp"
		command "heatLevelDown"
		command "coolLevelUp"
		command "coolLevelDown"
		command "updateDeviceData", ["string"]
		command "thermostatOperatingState", ["string"]
		command "setThermostatMode"
		command "setThermostatFanMode"
		command "setHeatingSetpoint"
		command "setCoolingSetpoint"
		command "changeMode"
		command "changeFanMode"
        
		attribute "thermostatMode", "string"
		attribute "alert", "string"

	}

	simulator { }

	tiles(scale: 2) {      
              
		multiAttributeTile(name:"tempSummary", type:"thermostat", width:6, height:4) {
			tileAttribute("device.temperature", key: "PRIMARY_CONTROL") {
				attributeState("temp", label:'${currentValue}°', unit:"dF", defaultState: true, backgroundColors: [
					// Celsius Color Range
					[value: 15, color: "#153591"],
					[value: 20, color: "#1e9cbb"],
					[value: 22, color: "#90d2a7"],
					[value: 24, color: "#44b621"],
					[value: 27, color: "#f1d801"],
					[value: 33, color: "#d04e00"],
					[value: 36, color: "#bc2323"],
					// Fahrenheit Color Range
					[value: 60, color: "#153591"],
					[value: 64, color: "#1e9cbb"],
					[value: 68, color: "#90d2a7"],
					[value: 74, color: "#44b621"],
					[value: 80, color: "#f1d801"],
					[value: 87, color: "#d04e00"],
					[value: 92, color: "#bc2323"]
				])
			}

			tileAttribute("device.inUse", key: "SECONDARY_CONTROL") {
				attributeState("Active", label:'Active')
				attributeState("Idle", label:'Idle')
			}

			tileAttribute("device.heatingSetpoint", key: "HEATING_SETPOINT") {
				attributeState("default", label:'${currentValue}', unit:"dF")
			}

			tileAttribute("device.coolingSetpoint", key: "COOLING_SETPOINT") {
				attributeState("default", label:'${currentValue}', unit:"dF")
			}

        } // End multiAttributeTile
	
		standardTile("coolLevelUp", "device.switch", canChangeIcon: false, decoration: "flat" ) {
			state("coolLevelUp",   action:"coolLevelUp",  label:"Cool", icon:"st.thermostat.thermostat-up")
		}  
		standardTile("coolLevelDown", "device.switch", canChangeIcon: false, decoration: "flat") {
			state("coolLevelDown", action:"coolLevelDown", label:"Cool", icon:"st.thermostat.thermostat-down")
		}

  	valueTile("coolingSetpoint", "device.coolingSetpoint", inactiveLabel: false, width: 2, height: 2) {
			state("coolingSetpoint", label:'${currentValue}°', backgroundColor:"#1e9cbb")
		}
        
		valueTile("heatingSetpoint", "device.heatingSetpoint", inactiveLabel: false, width: 2, height: 2) {
			state("heatingSetpoint", label:'${currentValue}°', backgroundColor:"#d04e00")
		}
        
		standardTile("heatLevelUp", "device.switch", canChangeIcon: false, decoration: "flat" ) {
			state("heatLevelUp",   action:"heatLevelUp",  label:"Heat", icon:"st.thermostat.thermostat-up")
		}  

		standardTile("heatLevelDown", "device.switch", canChangeIcon: false, decoration: "flat") {
			state("heatLevelDown", action:"heatLevelDown", label:"Heat",icon:"st.thermostat.thermostat-down")
		}
		

		standardTile("switch", "device.switch", canChangeIcon: false, decoration: "flat" ) {
			state "on", label: 'On', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#79b821"
			state("off", label: 'Off', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff")
		}
        
		standardTile("refresh", "device.switch", decoration: "flat", width: 2, height: 2) {
			state("default", action:"refresh.refresh",        icon:"st.secondary.refresh")
		}
		
		
    standardTile("thermostatMode", "device.thermostatMode", inactiveLabel:true, decoration: "flat", width: 2, height: 2) {
			state "default", 	label:'[thermostatMode]'
			state "Auto", 		label:'', icon:"st.thermostat.auto", action:"changeMode", nextState: "updating"
			state "Heating", 	label:'', icon:"st.thermostat.heating", action:"changeMode", nextState: "updating"
			state "Cooling", 	label:'', icon:"st.thermostat.cooling", action:"changeMode", nextState: "updating"
			state "Fan Only", label:'', icon:"st.thermostat.fan-on", action:"changeMode", nextState: "updating"
			state "Off", 			label:'', icon:"st.thermostat.heating-cooling-off", action:"changeMode", nextState: "updating"
			state("updating", label:"", icon: "https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/cmd_working.png")
    }

		standardTile("iconTile", "device.temperature", decoration: "flat", width: 1, height: 1, canChangeIcon: true) {
 			state "default", label:'${currentValue}°', icon: "st.Weather.weather2", backgroundColor: "#79B821"
 		}
        
    valueTile("outdoorTemperature", "device.outdoorTemperature", inactiveLabel: false, width: 1, height: 1) {
			state("outdoorTemperature", label:'${currentValue}°')
		}

		valueTile("deviceHumidity", "device.humidity", inactiveLabel: false, width: 1, height: 1) {
			state("deviceHumidity", label:'${currentValue}%', unit:"%")
		}

		standardTile("alert", "device.alert",  decoration: "flat", inactiveLabel: false) {
			state "true", icon: "st.alarm.water.wet"
			state "false",icon: "st.alarm.water.dry"
		}		

	 	standardTile("thermostatFanMode", "device.thermostatFanMode", inactiveLabel:true, decoration: "flat", width: 1, height: 1) {
			state "default", 	label:'[thermostatFanMode]'
			state "Low", 			label:'Low', icon:"st.vents.vents-open", action:"changeFanMode", nextState: "updating"
			state "Med.Lo", 	label:'Med.Lo', icon:"st.vents.vents-open", action:"changeFanMode", nextState: "updating"
			state "Medium", 	label:'Med', icon:"st.vents.vents-open", action:"changeFanMode", nextState: "updating"
			state "Med.Hi", 	label:'Med.Hi', icon:"st.vents.vent-open", action:"changeFanMode", nextState: "updating"
			state "High", 		label:'Hi', icon:"st.vents.vents-open", action:"changeFanMode", nextState: "updating"
			state "Off", 			label:'Off', icon:"st.vents.vent", action:"changeFanMode", nextState: "updating"
			state("updating", label:"", icon: "https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/cmd_working.png")
		}	 
		
        
	main (["iconTile"])
		details(["tempSummary", "coolLevelUp","coolingSetpoint", "heatingSetpoint", "heatLevelUp", "coolLevelDown", ,"heatLevelDown", "thermostatMode", "outdoorTemperature", "alert", "refresh", "deviceHumidity", "thermostatFanMode" ])
	}
}

def parse(String description) { }

def refresh() {
	log.debug "refresh"
	parent.refresh()
    //poll()
}

void changeMode() {
	def currentMode = device.currentState("thermostatMode")?.stringValue
	def lastTriedMode = currentMode ?: "Off"
	def modeOrder = ['Auto', 'Heating', 'Cooling', 'Fan Only', 'Off']
	def next = { modeOrder[modeOrder.indexOf(it) + 1] ?: modeOrder[0] }
	def nextMode = next(lastTriedMode)
	setThermostatMode(nextMode)
}

void changeFanMode() {
	def currentFanMode = device.currentState("thermostatFanMode")?.stringValue
	def lastTriedFanMode = currentFanMode ?: "Off"
	def fanModeOrder = ['Low', 'Med.Lo', 'Medium', 'Med.Hi', 'High', 'Off']
	def nextF = { fanModeOrder[fanModeOrder.indexOf(it) + 1] ?: fanModeOrder[0] }
	def nextFanMode = nextF(lastTriedFanMode)
	setThermostatFanMode(nextFanMode)

}

def setThermostatMode(String thermostatMode) {
	switch(thermostatMode){
		case "heat":
			thermostatMode = 'Heating'
			break
		case "cool":
			thermostatMode = 'Cooling'
			break
		case "auto":
			thermostatMode = 'Auto'
			break
		case "fan only":
			thermostatMode = 'Fan Only'
			break
		case "off":
			thermostatMode = 'Off'
			break
	}
	sendEvent(name: "thermostatMode", value: thermostatMode)
	parent.setDeviceMode(this.device, thermostatMode)
}

def setThermostatFanMode(String thermostatFanMode) {
	sendEvent(name: "thermostatFanMode", value: thermostatFanMode)
	parent.setFanMode(this.device, thermostatFanMode)	
	log.info "setThermostatFanMode: $thermostatFanMode " 
}

def setHeatingSetpoint(Number heatSetPoint) {
	sendEvent(name: "heatingSetpoint", value: heatSetPoint, unit: "F")
	parent.setHeatSetPoint(this.device, heatSetPoint)
}

def setCoolingSetpoint(Number coolSetPoint) {
	sendEvent(name: "coolingSetpoint", value: coolSetPoint, unit: "F")
	parent.setCoolSetPoint(this.device, coolSetPoint)
}

def heatLevelUp() { 
	def heatSetPoint = device.currentValue("heatingSetpoint")
	heatSetPoint = heatSetPoint + 1
	setHeatingSetpoint(heatSetPoint)
}	

def heatLevelDown() { 
	def heatSetPoint = device.currentValue("heatingSetpoint")
	heatSetPoint = heatSetPoint - 1
	setHeatingSetpoint(heatSetPoint)
}

def coolLevelUp() { 
	def coolSetPoint = device.currentValue("coolingSetpoint")
	coolSetPoint = coolSetPoint + 1
	setCoolingSetpoint(coolSetPoint)
}	

def coolLevelDown() { 
	def coolSetPoint = device.currentValue("coolingSetpoint")
	coolSetPoint = coolSetPoint - 1
	setCoolingSetpoint(coolSetPoint)
}

def updateDeviceData(data) {
	sendEvent(name: "heatingSetpoint", value: data.heatSetPoint, unit: "F")
	sendEvent(name: "minHeatSetPoint", value: data.minHeatSetPoint, unit: "F")
	sendEvent(name: "maxHeatSetPoint", value: data.maxHeatSetPoint, unit: "F")
	sendEvent(name: "coolingSetpoint", value: data.coolSetPoint, unit: "F")
	sendEvent(name: "minCoolSetPoint", value: data.minCoolSetPoint, unit: "F")
	sendEvent(name: "maxCoolSetPoint", value: data.maxCoolSetPoint, unit: "F")
	sendEvent(name: "switch", value: data.isEnabled ? "on" : "off")
	sendEvent(name: "humidity", value: (String.format("%3.0f",data.indoorHumidityPercentage)), unit: "%")
	sendEvent(name: "temperature", value: data.indoorTemperature, unit: "F")
	sendEvent(name: "outdoorTemperature", value: data.outdoorTemperature, unit: "F")
	sendEvent(name: "thermostatMode", value: data.mode)
	sendEvent(name: "thermostatFanMode", value: data.fanMode)
	sendEvent(name: "fanSpeed", value: data.fanSpeed)
	sendEvent(name: "inUse", value: data.inUse ? "Active" : "Idle")
	sendEvent(name: "alert", value: data.hasCriticalAlert ? "true" : "false")

}
