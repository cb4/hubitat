/**
 * cb4_lights_installer
 *
 * Hubitat Elevation user app; inventory and room names from wiring.xlsx.
 * Includes brain1-brain6 (40 production lights) and brain7 (4 test lights).
 * Requires the WLED cb4 driver (namespace "cb4", name "WLED cb4").
 *
 * Segment indices are embedded from the workbook Data outputs: LED1=0,
 * LED2=1, etc. Hubitat device names and network IDs use ID-brainN; labels
 * retain the workbook ID, mapped room, and description.
 *
 * Creates app child devices with IDs tied to lighting ID so that changes
 * to the assigned brain/controller retain their Hubitat device identity.
 * Installation is paced at one device every 8 seconds to reduce WLED load.
 *
 * Author: Conrad Bell IV (cb4)
 * Date:   2026-11-15*/

definition(name: "cb4_lights_installer",
        namespace: "cb4",
        author: "CB4",
        description: "Install/update WLED segment devices from the wiring workbook",
        category: "Convenience",
        singleInstance: true)

preferences {
    page(name: "mainPage")
}

// Hubitat resolves this page by the name declared in preferences.
@SuppressWarnings('GroovyUnusedDeclaration')
def mainPage() {
    dynamicPage(name: "mainPage", title: "cb4_lights_installer",
            refreshInterval: (state.running == true ? 5 : 0),
            install: true, uninstall: true) {
        section("Wiring inventory") {
            paragraph "44 lights: brain1=7, brain2=6, brain3=7, brain4=7, brain5=6, brain6=7, brain7=4." + " Wiring room names are translated using the workbook room map."
            input name: "operation", type: "enum", title: "Operation",
                    options: ["preview": "Preview and validate",
                              "install": "Create or update devices",
                              "delete" : "Delete all installer lights"],
                    defaultValue: "preview", required: true, submitOnChange: true
            input name: "runInstaller", type: "button",
                    title: "Run Selected Operation"
            paragraph "The device naming scheme is room_ID-brainN. Lights can be created while a brain is offline "
            + "by mapping brain controller pinouts to WLED segments, e.g., brain LED1 = WLED Segment 0, etc."
            + " Existing installer devices are reconfigured and reported. "
            + "Delete removes all child lights owned by this app. Hubitat doesn't have a "
            + "way to programmatically assign devices to Rooms; this must be done manually. For "
            + "troubleshooting, room names appear in labels/logs."
            if (state.lastResult) paragraph "Status: ${state.lastResult}"
            if (state.running == true) {
                paragraph "Progress: ${state.nextIndex ?: 0} / ${state.total ?: 0} complete"
            }
        }
    }
}

// Hubitat invokes these lifecycle callbacks by convention.
@SuppressWarnings('GroovyUnusedDeclaration')
def installed() { log.info "cb4_lights_installer installed. Run Preview and validate." }

@SuppressWarnings('GroovyUnusedDeclaration')
def updated() { log.info "cb4_lights_installer settings saved." }

// Hubitat dispatches app button events to this handler by convention.
@SuppressWarnings('GroovyUnusedDeclaration')
void appButtonHandler(String buttonName) {
    if (buttonName == "runInstaller") runInstaller()
}

private static List<Map> inventory() {
    // Hubitat names are taken from wiring.xlsx, "Hubitat Room Name Map".
    return [[brain: 'brain1', ip: '10.0.0.50', lights: [[id: '5.2', room: 'Dining Room', description: 'ceiling middle', led: 'LED1', segment: 0, sheetRow: 114],
                                                        [id: '2.1', room: 'Kitchen', description: 'west aisle', led: 'LED2', segment: 1, sheetRow: 115],
                                                        [id: '2.2', room: 'Kitchen', description: 'middle aisle', led: 'LED3', segment: 2, sheetRow: 116],
                                                        [id: '2.3', room: 'Kitchen', description: 'east aisle', led: 'LED4', segment: 3, sheetRow: 117],
                                                        [id: '9.1', room: 'Guest Bedroom', description: 'ceiling west', led: 'LED5', segment: 4, sheetRow: 118],
                                                        [id: '9.2', room: 'Guest Bedroom', description: 'ceiling east', led: 'LED6', segment: 5, sheetRow: 119],
                                                        [id: '10.1', room: 'Master Bedroom', description: 'window inside curtain', led: 'LED7', segment: 6, sheetRow: 120],]],
            [brain: 'brain2', ip: '10.0.0.51', lights: [[id: '4.1', room: 'Living Room', description: 'circle north', led: 'LED1', segment: 0, sheetRow: 128],
                                                        [id: '4.2', room: 'Living Room', description: 'circle east', led: 'LED2', segment: 1, sheetRow: 129],
                                                        [id: '10.01', room: 'Master Bedroom', description: 'entry', led: 'LED3', segment: 2, sheetRow: 130],
                                                        [id: '10.02', room: 'Master Bedroom', description: 'walkway', led: 'LED4', segment: 3, sheetRow: 131],
                                                        [id: '11.3', room: 'Master Bath', description: 'shower', led: 'LED5', segment: 4, sheetRow: 132],
                                                        [id: '5.1', room: 'Dining Room', description: 'ceiling west', led: 'LED6', segment: 5, sheetRow: 133],]],
            [brain: 'brain3', ip: '10.0.0.52', lights: [[id: '8.1', room: 'Den', description: 'ceiling north', led: 'LED1', segment: 0, sheetRow: 141],
                                                        [id: '8.2', room: 'Den', description: 'ceiling east', led: 'LED2', segment: 1, sheetRow: 142],
                                                        [id: '8.4', room: 'Den', description: 'ceiling west', led: 'LED3', segment: 2, sheetRow: 143],
                                                        [id: '7.2', room: 'Guest Bath', description: 'shower', led: 'LED4', segment: 3, sheetRow: 144],
                                                        [id: '6.1', room: 'Hall', description: 'ceiling east', led: 'LED5', segment: 4, sheetRow: 145],
                                                        [id: '3.1', room: 'Laundry', description: 'hall ceiling', led: 'LED6', segment: 5, sheetRow: 146],
                                                        [id: '10.13', room: 'Master Bedroom', description: 'S baseboard', led: 'LED7', segment: 6, sheetRow: 147],]],
            [brain: 'brain4', ip: '10.0.0.53', lights: [[id: '2.5', room: 'Kitchen', description: 'S wall baseboard', led: 'LED1', segment: 0, sheetRow: 155],
                                                        [id: '2.6', room: 'Kitchen', description: 'broom closet', led: 'LED2', segment: 1, sheetRow: 156],
                                                        [id: '2.7', room: 'Kitchen', description: 'SE+E+NE island base', led: 'LED3', segment: 2, sheetRow: 157],
                                                        [id: '2.8', room: 'Kitchen', description: 'SW+W+NW island base', led: 'LED4', segment: 3, sheetRow: 158],
                                                        [id: '11.5', room: 'Master Bath', description: 'makeup station baseboard', led: 'LED5', segment: 4, sheetRow: 159],
                                                        [id: '8.3', room: 'Den', description: 'ceiling south', led: 'LED6', segment: 5, sheetRow: 160],
                                                        [id: '4.3', room: 'Living Room', description: 'circle south', led: 'LED7', segment: 6, sheetRow: 161],]],
            [brain: 'brain5', ip: '10.0.0.54', lights: [[id: '8.5', room: 'Den', description: 'window inside curtain', led: 'LED1', segment: 0, sheetRow: 169],
                                                        [id: '8.6', room: 'Den', description: 'window outside curtain', led: 'LED2', segment: 1, sheetRow: 170],
                                                        [id: '4.5', room: 'Living Room', description: 'window inside curtain', led: 'LED3', segment: 2, sheetRow: 171],
                                                        [id: '4.6', room: 'Living Room', description: 'window outside curtain', led: 'LED4', segment: 3, sheetRow: 172],
                                                        [id: '11.4', room: 'Master Bath', description: 'shower niche', led: 'LED5', segment: 4, sheetRow: 173],
                                                        [id: '11.6', room: 'Master Bath', description: 'vanity baseboard', led: 'LED6', segment: 5, sheetRow: 174],]],
            [brain: 'brain6', ip: '10.0.0.55', lights: [[id: '13.1', room: 'Balcony', description: 'balcony west', led: 'LED1', segment: 0, sheetRow: 181],
                                                        [id: '7.3', room: 'Guest Bath', description: 'niche', led: 'LED2', segment: 1, sheetRow: 182],
                                                        [id: '7.4', room: 'Guest Bath', description: 'baseboard', led: 'LED3', segment: 2, sheetRow: 183],
                                                        [id: '9.6', room: 'Guest Bedroom', description: 'window inside curtain', led: 'LED4', segment: 3, sheetRow: 184],
                                                        [id: '9.7', room: 'Guest Bedroom', description: 'window outside curtain', led: 'LED5', segment: 4, sheetRow: 185],
                                                        [id: '10.11', room: 'Master Bedroom', description: 'window outside curtain', led: 'LED6', segment: 5, sheetRow: 186],
                                                        [id: '10.12', room: 'Master Bedroom', description: 'N baseboard', led: 'LED7', segment: 6, sheetRow: 187],]],
            [brain: 'brain7', ip: '10.0.0.56', lights: [[id: '99.1', room: 'test', description: 'test light 1', led: 'LED1', segment: 0, sheetRow: 195],
                                                        [id: '99.2', room: 'test', description: 'test light 2', led: 'LED2', segment: 1, sheetRow: 196],
                                                        [id: '99.3', room: 'test', description: 'test light 3', led: 'LED3', segment: 2, sheetRow: 197],
                                                        [id: '99.4', room: 'test', description: 'test light 4', led: 'LED4', segment: 3, sheetRow: 198],]],]
}

private List<Map> buildPlan() {
    List<Map> targets = []
    List<Map> configs = inventory()
    int expected = configs.sum { it.lights.size() } as Integer
    if (expected != 44) throw new IllegalStateException("Inventory changed: ${expected} vs 44")
    Set<String> usedDeviceIds = [] as Set

    configs.each { cfg ->
        List<Map> thisBrain = []
        Set<Integer> usedSegments = [] as Set
        List<String> problems = []
        cfg.lights.each { light ->
            String lightId = light.id as String
            String dni = "${lightId}-${cfg.brain}"
            if (!lightId?.trim()) {
                problems.add("${cfg.brain}: blank workbook light ID")
                return
            }
            if (!usedDeviceIds.add(dni)) {
                problems.add("${lightId}: duplicate light ID in wiring")
                return
            }
            def ledMatch = (light.led as String) =~ /^LED([1-9]\d*)$/
            if (!ledMatch.matches()) {
                problems.add("${lightId}: invalid workbook Data value '${light.led}'")
                return
            }
            int calculatedSegment = ledMatch.group(1).toInteger() - 1
            int segmentId = light.segment as Integer
            if (segmentId != calculatedSegment) {
                problems.add("${lightId}: embedded segment ${segmentId} does not match ${light.led} (expected ${calculatedSegment})")
                return
            }
            if (!usedSegments.add(segmentId)) {
                problems.add("${lightId}: segment ${segmentId} assigned twice on ${cfg.brain}")
                return
            }
            thisBrain << [dni        : dni, brain: cfg.brain, ip: cfg.ip,
                          lightId    : lightId, room: light.room,
                          description: light.description, led: light.led,
                          segment    : segmentId, mapping: 'embedded from workbook Data column']
        }
        if (problems) {
            log.error "${cfg.brain}: SKIPPED (${problems.size()} inventory errors)"
            problems.each { log.error "  ${cfg.brain}: ${it}" }
        } else if (thisBrain.size() == cfg.lights.size()) {
            targets.addAll(thisBrain)
        }
    }
    return targets
}

// Invoked from appButtonHandler.
void runInstaller() {
    unschedule("installNext")
    unschedule("deleteNext")
    state.running = false
    state.pending = []
    state.deletePending = []
    state.nextIndex = 0
    state.total = 0
    state.tally = [created: 0, updated: 0, deleted: 0, failed: 0]

    if (settings.operation == "delete") {
        state.deletePending = (getChildDevices() ?: []).collect { child -> [dni: child.deviceNetworkId, label: child.label ?: child.name]
        }
        state.total = state.deletePending.size()
        if (state.total == 0) {
            state.lastResult = "No installer lights to delete."
            return
        }
        state.running = true
        state.lastResult = "Deleting ${state.total} installer lights..."
        runIn(1, "deleteNext")
        return
    }

    List<Map> targets = buildPlan()
    Map counts = targets.countBy { it.brain }
    log.info "cb4_lights_installer: prepared ${targets.size()} / 44 lights. By brain: ${counts}"
    targets.each { t -> log.info "${t.lightId} | ${t.room} | ${t.description} | ${t.brain}" + " ${t.ip} | ${t.led} -> WLED segment ${t.segment} (${t.mapping})"
    }
    if (settings.operation != "install") {
        state.lastResult = "Preview ready: ${targets.size()} of 44 lights. By brain: ${counts}."
        return
    }
    if (targets.isEmpty()) {
        state.lastResult = "No devices created: the embedded inventory did not pass validation."
        return
    }
    state.pending = targets
    state.total = targets.size()
    state.running = true
    state.lastResult = "Starting ${state.total} lights. By brain: ${counts}."
    runIn(1, "installNext")
}

// Hubitat invokes scheduled callbacks registered by runIn using their method names.
@SuppressWarnings('GroovyUnusedDeclaration')
void deleteNext() {
    List<Map> pending = (state.deletePending ?: []) as List<Map>
    int index = (state.nextIndex ?: 0) as Integer
    if (index >= pending.size()) {
        Map tally = state.tally ?: [:]
        state.running = false
        state.lastResult = "Delete finished: deleted ${tally.deleted ?: 0}, failed ${tally.failed ?: 0}."
        state.deletePending = []
        log.info "cb4_lights_installer: ${state.lastResult}"
        return
    }
    Map item = pending[index]
    Map tally = state.tally ?: [created: 0, updated: 0, deleted: 0, failed: 0]
    try {
        deleteChildDevice(item.dni as String)
        tally.deleted = (tally.deleted ?: 0) + 1
        log.info "Deleted installer light ${item.dni} (${item.label})"
        state.lastResult = "Deleting ${index + 1} / ${pending.size()}: ${item.dni}"
    } catch (Exception error) {
        tally.failed = (tally.failed ?: 0) + 1
        state.lastResult = "Delete failed for ${item.dni}: ${error.message}"
        log.error state.lastResult
    }
    state.tally = tally
    state.nextIndex = index + 1
    runIn(1, "deleteNext")
}

@SuppressWarnings('GroovyUnusedDeclaration')
void installNext() {
    List<Map> pending = (state.pending ?: []) as List<Map>
    int index = (state.nextIndex ?: 0) as Integer
    if (index >= pending.size()) {
        Map tally = state.tally ?: [:]
        state.running = false
        state.lastResult = "Finished: created ${tally.created ?: 0}, " + "updated ${tally.updated ?: 0}, failed ${tally.failed ?: 0}."
        log.info "cb4_lights_installer: ${state.lastResult}"
        state.pending = []
        return
    }
    Map target = pending[index]
    Map tally = state.tally ?: [created: 0, updated: 0, failed: 0]
    String itemStatus
    try {
        String deviceName = "${target.lightId}-${target.brain}"
        String label = "${target.lightId}-${target.brain}"
        def child = getChildDevice(target.dni as String)
        boolean exists = (child != null)
        if (!exists) {
            child = addChildDevice("cb4", "WLED cb4", target.dni as String,
                    [name: deviceName, label: label, isComponent: false])
        } else {
            child.setName(label)
            child.setLabel(label)
            log.info "REDEFINING existing Hubitat light ${target.dni}: updating driver settings and label."
        }
        child.updateSetting("uri", [value: "http://${target.ip}", type: "text"])
        child.updateSetting("ledSegment", [value: "${target.segment}", type: "number"])
        child.updateSetting("transitionTime", [value: "700", type: "enum"])
        child.updateSetting("refreshInterval", [value: "300", type: "enum"])
        child.updateSetting("powerOffParent", [value: "false", type: "bool"])
        // Driver retries failed commands three times; disable its repeated 30s probe loop.
        child.updateSetting("enableRetry", [value: "true", type: "bool"])
        child.updateSetting("connectionMonitoring", [value: "false", type: "bool"])
        child.updateSetting("logEnable", [value: "false", type: "bool"])
        child.updated() // Apply preferences, configure polling and refresh state.
        String action = exists ? "updated" : "created"
        tally[action] = (tally[action] ?: 0) + 1
        itemStatus = "${action}: ${label}"
        log.info "${itemStatus} | ${target.ip} | ${target.led} | segment ${target.segment}"
    } catch (Exception error) {
        tally.failed = (tally.failed ?: 0) + 1
        itemStatus = "FAILED ${target.lightId}-${target.brain}: ${error.message}"
        log.error itemStatus
    }
    state.tally = tally
    state.nextIndex = index + 1
    state.lastResult = "Installing ${index + 1} / ${pending.size()}: ${itemStatus}. " + "Created ${tally.created ?: 0}, updated ${tally.updated ?: 0}, failed ${tally.failed ?: 0}."
    runIn(8, "installNext")
}
