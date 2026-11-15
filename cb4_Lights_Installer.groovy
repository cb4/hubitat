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
            paragraph "44 lights: brain1=7, brain2=6, brain3=7, brain4=7, brain5=6, brain6=7, brain7=4." +
                    " Wiring room names are translated using the workbook room map."
            input name: "operation", type: "enum", title: "Operation",
                    options: ["preview": "Preview and validate",
                              "install": "Create or update devices",
                              "delete" : "Delete all installer lights"],
                    defaultValue: "preview", required: true, submitOnChange: true
            input name: "runInstaller", type: "button",
                    title: "Run Selected Operation"
            String operationHelp = "The device naming scheme is room_ID-brainN. Lights can be created " +
                    "while a brain is offline " +
                    "by mapping brain controller pinouts to WLED segments, e.g., brain LED1 = WLED Segment 0, etc." +
                    " Existing installer devices are reconfigured and reported. " +
                    "Delete removes all child lights owned by this app. Hubitat doesn't have a " +
                    "way to programmatically assign devices to Rooms; this must be done manually. For " +
                    "troubleshooting, room names appear in labels/logs."
            paragraph operationHelp
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
    return [
        [brain: 'brain1', ip: '10.0.0.50', lights: [
            light('5.2', 'Dining Room', 'ceiling middle', 'LED1', 0, 114),
            light('2.1', 'Kitchen', 'west aisle', 'LED2', 1, 115),
            light('2.2', 'Kitchen', 'middle aisle', 'LED3', 2, 116),
            light('2.3', 'Kitchen', 'east aisle', 'LED4', 3, 117),
            light('9.1', 'Guest Bedroom', 'ceiling west', 'LED5', 4, 118),
            light('9.2', 'Guest Bedroom', 'ceiling east', 'LED6', 5, 119),
            light('10.10', 'Master Bedroom', 'window inside curtain', 'LED7', 6, 120)
        ]],
        [brain: 'brain2', ip: '10.0.0.51', lights: [
            light('4.1', 'Living Room', 'circle north', 'LED1', 0, 128),
            light('4.2', 'Living Room', 'circle east', 'LED2', 1, 129),
            light('10.01', 'Master Bedroom', 'entry', 'LED3', 2, 130),
            light('10.02', 'Master Bedroom', 'walkway', 'LED4', 3, 131),
            light('11.3', 'Master Bath', 'shower', 'LED5', 4, 132),
            light('5.1', 'Dining Room', 'ceiling west', 'LED6', 5, 133)
        ]],
        [brain: 'brain3', ip: '10.0.0.52', lights: [
            light('8.1', 'Den', 'ceiling north', 'LED1', 0, 141),
            light('8.2', 'Den', 'ceiling east', 'LED2', 1, 142),
            light('8.4', 'Den', 'ceiling west', 'LED3', 2, 143),
            light('7.2', 'Guest Bath', 'shower', 'LED4', 3, 144),
            light('6.1', 'Hall', 'ceiling east', 'LED5', 4, 145),
            light('3.1', 'Laundry', 'hall ceiling', 'LED6', 5, 146),
            light('10.13', 'Master Bedroom', 'S baseboard', 'LED7', 6, 147)
        ]],
        [brain: 'brain4', ip: '10.0.0.53', lights: [
            light('2.5', 'Kitchen', 'S wall baseboard', 'LED1', 0, 155),
            light('2.6', 'Kitchen', 'broom closet', 'LED2', 1, 156),
            light('2.7', 'Kitchen', 'SE+E+NE island base', 'LED3', 2, 157),
            light('2.8', 'Kitchen', 'SW+W+NW island base', 'LED4', 3, 158),
            light('11.5', 'Master Bath', 'makeup station baseboard', 'LED5', 4, 159),
            light('8.3', 'Den', 'ceiling south', 'LED6', 5, 160),
            light('4.3', 'Living Room', 'circle south', 'LED7', 6, 161)
        ]],
        [brain: 'brain5', ip: '10.0.0.54', lights: [
            light('8.5', 'Den', 'window inside curtain', 'LED1', 0, 169),
            light('8.6', 'Den', 'window outside curtain', 'LED2', 1, 170),
            light('4.5', 'Living Room', 'window inside curtain', 'LED3', 2, 171),
            light('4.6', 'Living Room', 'window outside curtain', 'LED4', 3, 172),
            light('11.4', 'Master Bath', 'shower niche', 'LED5', 4, 173),
            light('11.6', 'Master Bath', 'vanity baseboard', 'LED6', 5, 174)
        ]],
        [brain: 'brain6', ip: '10.0.0.55', lights: [
            light('13.1', 'Balcony', 'balcony west', 'LED1', 0, 181),
            light('7.3', 'Guest Bath', 'niche', 'LED2', 1, 182),
            light('7.4', 'Guest Bath', 'baseboard', 'LED3', 2, 183),
            light('9.6', 'Guest Bedroom', 'window inside curtain', 'LED4', 3, 184),
            light('9.7', 'Guest Bedroom', 'window outside curtain', 'LED5', 4, 185),
            light('10.11', 'Master Bedroom', 'window outside curtain', 'LED6', 5, 186),
            light('10.12', 'Master Bedroom', 'N baseboard', 'LED7', 6, 187)
        ]],
        [brain: 'brain7', ip: '10.0.0.56', lights: [
            light('99.1', 'test', 'test light 1', 'LED1', 0, 195),
            light('99.2', 'test', 'test light 2', 'LED2', 1, 196),
            light('99.3', 'test', 'test light 3', 'LED3', 2, 197),
            light('99.4', 'test', 'test light 4', 'LED4', 3, 198)
        ]]
    ]
}

private static Map light(String id, String room, String description, String led,
                         int segment, int sheetRow) {
    return [id: id, room: room, description: description,
            led: led, segment: segment, sheetRow: sheetRow]
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
                problems.add("${lightId}: embedded segment ${segmentId} does not match " +
                        "${light.led} (expected ${calculatedSegment})")
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
        state.deletePending = (getChildDevices() ?: []).collect { child ->
            [dni: child.deviceNetworkId, label: child.label ?: child.name]
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
    targets.each { t ->
        log.info "${t.lightId} | ${t.room} | ${t.description} | ${t.brain} " +
                "${t.ip} | ${t.led} -> WLED segment ${t.segment} (${t.mapping})"
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
        state.lastResult = "Finished: created ${tally.created ?: 0}, " +
                "updated ${tally.updated ?: 0}, failed ${tally.failed ?: 0}."
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
    state.lastResult = "Installing ${index + 1} / ${pending.size()}: ${itemStatus}. " +
            "Created ${tally.created ?: 0}, updated ${tally.updated ?: 0}, " +
            "failed ${tally.failed ?: 0}."
    runIn(8, "installNext")
}
