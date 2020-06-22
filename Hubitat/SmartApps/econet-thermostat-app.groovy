/**
 *  Rheem EcoNet (Connect)
 *
 * Contributors:
 * Justin Huff
 * fsrt
 * Brian Spranger
 * copyninja
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *  Last Updated : 4/20/18
 *
 *
 */
definition(
    name: "Rheem EcoNet Thermostat",
    namespace: "bmcgair",
    author: "B McGair",
    description: "Connect to Rheem EcoNet Thermostat",
    category: "SmartThings Labs",
    iconUrl: "http://smartthings.copyninja.net/icons/Rheem_EcoNet@1x.png",
    iconX2Url: "http://smartthings.copyninja.net/icons/Rheem_EcoNet@2x.png",
    iconX3Url: "http://smartthings.copyninja.net/icons/Rheem_EcoNet@3x.png")


preferences {
	page(name: "prefLogIn", title: "Rheem EcoNet")    
	page(name: "prefListDevice", title: "Rheem EcoNet")
}

/* Preferences */
def prefLogIn() {
	def showUninstall = username != null && password != null 
	return dynamicPage(name: "prefLogIn", title: "Connect to Rheem EcoNet", nextPage:"prefListDevice", uninstall:showUninstall, install: false) {
		section("Login Credentials"){
			input("username", "email", title: "Username", description: "Rheem EcoNet Email")
			input("password", "password", title: "Password", description: "Rheem EcoNet password (case sensitive)")
		} 
	}
}

def prefListDevice() {	
  login()
	if (login()) {
		def hvaclist = gethvaclist()
		if (hvaclist) {
			return dynamicPage(name: "prefListDevice",  title: "Devices", install:true, uninstall:true) {
				section("Select which thermostat to use"){
					input(name: "hvac", type: "enum", required:false, multiple:true, options:[hvaclist])
				}
			}
		} else {
			return dynamicPage(name: "prefListDevice",  title: "Error!", install:false, uninstall:true) {
				section(""){ paragraph "Could not find any devices"  }
			}
		}
	} else {
		return dynamicPage(name: "prefListDevice",  title: "Error!", install:false, uninstall:true) {
			section(""){ paragraph "The username or password you entered is incorrect. Try again. " }
		}  
	}
}


/* Initialization */
def installed() { initialize() }
def updated() { 
	unschedule()
	unsubscribe()
	initialize() 
	runEvery10Minutes(refresh)

}
def uninstalled() {
	unschedule()
  unsubscribe()
	getChildDevices().each { deleteChildDevice(it) }
}	

def initialize() {

	// Create selected devices
	def hvaclist = gethvaclist()
    def selectedDevices = [] + getSelectedDevices("hvac")
    selectedDevices.each {
    	def dev = getChildDevice(it)
        def name  = hvaclist[it]
        if (dev == null) {
	        try {
    			addChildDevice("bmcgair", "Rheem Econet Thermostat", it, null, ["name": "Rheem Econet: " + name])
    	    } catch (e)	{
				log.debug "addChildDevice Error: $e"
          	}
        }
    }
}

def getSelectedDevices( settingsName ) {
	def selectedDevices = []
	(!settings.get(settingsName))?:((settings.get(settingsName)?.getAt(0)?.size() > 1)  ? settings.get(settingsName)?.each { selectedDevices.add(it) } : selectedDevices.add(settings.get(settingsName)))
	return selectedDevices
}


/* Data Management */
// Listing all the HVAC Units you have in Rheem EcoNet
private gethvaclist() { 	 
	def deviceList = [:]
	apiGet("/locations", [] ) { response ->
    	if (response.status == 200) {
          	response.data.equipment[0].each { 
            	if (it.type.equals("HVAC")) {
                	deviceList["" + it.id]= it.name
                }
            }
        }
    }
    return deviceList
}

// Refresh data
def refresh() {
  if (!login()) {
      return
    }
	log.info "Refreshing data..."
    
	// get all the children and send updates
	getChildDevices().each {
    	def id = it.deviceNetworkId
    	apiGet("/equipment/$id", [] ) { response ->
    		if (response.status == 200) {
            	log.debug "Got data: $response.data"
            	it.updateDeviceData(response.data)
            }
        }

  }  
}

def setCoolSetPoint(childDevice, coolsetpoint) { 
	log.info "setDeviceSetPoint: $childDevice.deviceNetworkId $coolsetpoint" 
	if (login()) {
    	apiPut("/equipment/$childDevice.deviceNetworkId", [
        	body: [
                coolSetPoint: coolsetpoint,
            ]
        ])
    }
}
def setHeatSetPoint(childDevice, heatsetpoint) { 
	log.info "setDeviceSetPoint: $childDevice.deviceNetworkId $heatsetpoint" 
	if (login()) {
    	apiPut("/equipment/$childDevice.deviceNetworkId", [
        	body: [
                heatSetPoint: heatsetpoint,
            ]
        ])
    }
}
// available values are Heating, Cooling, Auto, Fan Only, Off, Emergency Heat
def setDeviceMode(childDevice, mode) {
	log.info "setDeviceMode: $childDevice.deviceNetworkId $mode" 
	if (login()) {
    	apiPut("/equipment/$childDevice.deviceNetworkId/modes", [
        	body: [
                mode: mode,
            ]
        ])
    }
}
// available values are Auto, Low, Med.Lo, Medium, Med.Hi, High
def setFanMode(childDevice, fanmode) {
	log.info "setFanMode: $childDevice.deviceNetworkId $fanmode" 
	if (login()) {
    	apiPut("/equipment/$childDevice.deviceNetworkId/fanModes", [
        	body: [
                fanMode: fanmode,
            ]
        ])
    }
}

private login() {
	def apiParams = [
    	uri: getApiURL(),
        path: "/auth/token",
        headers: ["Authorization": "Basic Y29tLnJoZWVtLmVjb25ldF9hcGk6c3RhYmxla2VybmVs"],
        requestContentType: "application/x-www-form-urlencoded",
        body: [
        	username: settings.username,
        	password: settings.password,
        	"grant_type": "password"
        ],
    ]
    if (state.session?.expiration < now()) {
    	try {
			httpPost(apiParams) { response -> 
            	if (response.status == 200) {
                	log.debug "Login good!"
                	state.session = [ 
                    	accessToken: response.data.access_token,
                    	refreshToken: response.data.refresh_token,
                    	expiration: now() + 150000
                	]
                	return true
            	} else {
                	return false
            	} 	
        	}
		}	catch (e)	{
			log.debug "API Error: $e"
        	return false
		}
	} else { 
    	// TODO: do a refresh 
		return true
	}
}

/* API Management */
// HTTP GET call
private apiGet(apiPath, apiParams = [], callback = {}) {	
	// set up parameters
	apiParams = [ 
		uri: getApiURL(),
		path: apiPath,
        headers: ["Authorization": getApiAuth()],
        requestContentType: "application/json",
	] + apiParams
	log.debug "GET: $apiParams"
	try {
		httpGet(apiParams) { response -> 
        	callback(response)
        }
	}	catch (e)	{
		log.debug "API Error: $e"
	}
}

// HTTP PUT call
private apiPut(apiPath, apiParams = [], callback = {}) {	
	// set up parameters
	apiParams = [ 
		uri: getApiURL(),
		path: apiPath,
        headers: ["Authorization": getApiAuth()],
        requestContentType: "application/json",
	] + apiParams
	
	try {
		httpPut(apiParams) { response -> 
        	callback(response)
        }
	}	catch (e)	{
		log.debug "API Error: $e"
	}
}

private getApiURL() { 
	return "https://econet-api.rheemcert.com"
}
    
private getApiAuth() {
	return "Bearer " + state.session?.accessToken
}
