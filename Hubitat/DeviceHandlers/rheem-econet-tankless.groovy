/**
 *  Rheem Econet Tankless Water Heater
 *
 * Contributors:
 *    https://github.com/copy-ninja/SmartThings_RheemEcoNet
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
 *  Last Updated : 2018-04-20
 *
 */
metadata {
	definition (name: "Rheem Econet Tankless", namespace: "bmcgair", author: "Bill McGair") {
        capability "Thermostat"
		capability "Actuator"
		capability "Refresh"
		capability "Sensor"
		capability "Thermostat Heating Setpoint"
    capability "Temperature Measurement"
		
		command "heatLevelUp"
		command "heatLevelDown"
    command "refresh"
		command "updateDeviceData", ["string"]
	}

	simulator { }

    tiles(scale: 2)  {
        multiAttributeTile(name:"thermostat", type:"thermostat", width:6, height:4) {

            tileAttribute("device.heatingSetpoint", key: "PRIMARY_CONTROL") {
                attributeState("temp", label:'${currentValue}°', unit:"dF", defaultState: true)
            }
            tileAttribute("", key: "VALUE_CONTROL") {
                attributeState("VALUE_UP", action: "heatLevelUp")
                attributeState("VALUE_DOWN", action: "heatLevelDown")
            }

            tileAttribute("device.thermostatOperatingState", key: "OPERATING_STATE") {
                attributeState("idle", label:'${name}', backgroundColor:"#1e9cbb")
                attributeState("heating", label:'${name}', backgroundColor:"#bc2323")
            }
        }

	    main("thermostat")
	}
}

def parse(String description) { }

def refresh() {
	log.debug "refresh"
	parent.refresh()
}

def setHeatingSetpoint(Number setPoint) {
   	sendEvent(name: "heatingSetpoint", value: setPoint, unit: "F")
	parent.setDeviceSetPoint(this.device, setPoint)
    refresh()
}

def heatLevelUp() { 
	def setPoint = device.currentValue("heatingSetpoint")
    setPoint = setPoint + 1
	setHeatingSetpoint(setPoint)
}	

def heatLevelDown() { 
	def setPoint = device.currentValue("heatingSetpoint")
    setPoint = setPoint - 1
    setHeatingSetpoint(setPoint)
}

def updateDeviceData(data) {
    sendEvent(name: "heatingSetpoint", value: data.setPoint, unit: "F")
    sendEvent(name: "thermostatOperatingState", value: data.inUse ? "heating" : "idle")

}
