/**
 * Brain Lights Installer – brain7-only test configuration
 * Generated from led lighting plan worksheet "wiring" tab.
 * Active: brain7 (10.0.0.56), IDs temp1-temp4, WLED segments 0-3.
 * brain1-brain6 are retained in source but commented out for testing.
 * Live WLED segment discovery/validation is commented out.
 * LED1=segment 0, LED2=segment 1, etc.
 * Hubitat device names and network IDs use lightID-brainN.
 * Requires installed WLED cb4 driver (namespace "cb4").
 * Stable device IDs are based on workbook light IDs.
 *
 * Author: cb4
 * Date:   2026-11-14
 * Naming update requested by project maintainer; implemented by OpenAI Codex, 2026-11-15.
 */
 
import groovy.json.JsonSlurper

definition(
    name: "Brain Lights Installer",
    namespace: "cb4",
    author: "CB4",
    description: "Install/update WLED light devices from the wiring workbook",
    category: "Convenience",
    singleInstance: true
)

preferences {
    page(name: "mainPage")
}

def mainPage() {
    dynamicPage(name: "mainPage", title: "Brain Lights Installer",
                install: true, uninstall: true) {
        section("Wiring inventory") {
            paragraph "TEST: brain7 (10.0.0.56), 4 lights (temp1-temp4). " +
                      "brain1-brain6 are commented out. Segment ID = LED number - 1."
            input name: "operation", type: "enum", title: "Operation",
                  options: ["preview": "Preview and validate",
                            "install": "Create or update devices"],
                  defaultValue: "preview", required: true, submitOnChange: true
            input name: "updateExistingLabels", type: "bool",
                  title: "Update labels of previously installed devices",
                  defaultValue: false, submitOnChange: true
            input name: "runInstaller", type: "button",
                  title: "Run Selected Operation"
            paragraph "Light names come from workbook ID column. " +
                      "Live WLED segment validation is disabled for the test."
            if (state.lastResult) paragraph "Last result: ${state.lastResult}"
        }
    }
}

def installed() { log.info "Brain Lights Installer installed. Run Preview and validate." }
def updated() { log.info "Brain Lights Installer settings saved." }

void appButtonHandler(String buttonName) {
    if (buttonName == "runInstaller") runInstaller()
}

private List<Map> inventory() {
    return [
        /* brain1-brain6 disabled for brain7-only test.
        [brain: 'brain1', ip: '10.0.0.50', lights: [
            [id: '5.2', room: 'dining', description: 'ceiling middle', led: 'LED1', power: 'Out 1', sheetRow: 114],
            [id: '2.1', room: 'kitchen', description: 'west aisle', led: 'LED2', power: 'Out 5', sheetRow: 115],
            [id: '2.2', room: 'kitchen', description: 'middle aisle', led: 'LED3', power: 'Out 9', sheetRow: 116],
            [id: '2.3', room: 'kitchen', description: 'east aisle', led: 'LED4', power: 'Out 3', sheetRow: 117],
            [id: '9.1', room: 'office', description: 'ceiling west', led: 'LED5', power: 'Out 7', sheetRow: 118],
            [id: '9.2', room: 'office', description: 'ceiling east', led: 'LED6', power: 'Out 11', sheetRow: 119],
            [id: '10.1', room: 'master', description: 'window inside curtain', led: 'LED7', power: 'Out 2', sheetRow: 120],
        ]],
        [brain: 'brain2', ip: '10.0.0.51', lights: [
            [id: '4.1', room: 'living', description: 'circle north', led: 'LED1', power: 'Out 1', sheetRow: 128],
            [id: '4.2', room: 'living', description: 'circle east', led: 'LED2', power: 'Out 5', sheetRow: 129],
            [id: '10.01', room: 'master', description: 'entry', led: 'LED3', power: 'Out 9', sheetRow: 130],
            [id: '10.02', room: 'master', description: 'walkway', led: 'LED4', power: 'Out 3', sheetRow: 131],
            [id: '11.3', room: 'master bath', description: 'shower', led: 'LED5', power: 'Out 7', sheetRow: 132],
            [id: '5.1', room: 'dining', description: 'ceiling west', led: 'LED6', power: 'Out 11', sheetRow: 133],
        ]],
        [brain: 'brain3', ip: '10.0.0.52', lights: [
            [id: '8.1', room: 'guest', description: 'ceiling north', led: 'LED1', power: 'Out 1', sheetRow: 141],
            [id: '8.2', room: 'guest', description: 'ceiling east', led: 'LED2', power: 'Out 5', sheetRow: 142],
            [id: '8.4', room: 'guest', description: 'ceiling west', led: 'LED3', power: 'Out 9', sheetRow: 143],
            [id: '7.2', room: 'guest bath', description: 'shower', led: 'LED4', power: 'Out 3', sheetRow: 144],
            [id: '6.1', room: 'hall', description: 'ceiling east', led: 'LED5', power: 'Out 7', sheetRow: 145],
            [id: '3.1', room: 'laundry', description: 'hall ceiling', led: 'LED6', power: 'Out 11', sheetRow: 146],
            [id: '10.13', room: 'master', description: 'S baseboard', led: 'LED7', power: 'Out 2', sheetRow: 147],
        ]],
        [brain: 'brain4', ip: '10.0.0.53', lights: [
            [id: '2.5', room: 'kitchen', description: 'S wall baseboard', led: 'LED1', power: 'Out 1', sheetRow: 155],
            [id: '2.6', room: 'kitchen', description: 'broom closet', led: 'LED2', power: 'Out 5', sheetRow: 156],
            [id: '2.7', room: 'kitchen', description: 'SE+E+NE island base', led: 'LED3', power: 'Out 9', sheetRow: 157],
            [id: '2.8', room: 'kitchen', description: 'SW+W+NW island base', led: 'LED4', power: 'Out 3', sheetRow: 158],
            [id: '11.5', room: 'master bath', description: 'makeup station baseboard', led: 'LED5', power: 'Out 7', sheetRow: 159],
            [id: '8.3', room: 'guest', description: 'ceiling south', led: 'LED6', power: 'Out 11', sheetRow: 160],
            [id: '4.3', room: 'living', description: 'circle south', led: 'LED7', power: 'Out 2', sheetRow: 161],
        ]],
        [brain: 'brain5', ip: '10.0.0.54', lights: [
            [id: '8.5', room: 'guest', description: 'window inside curtain', led: 'LED1', power: 'Out 1', sheetRow: 169],
            [id: '8.6', room: 'guest', description: 'window outside curtain', led: 'LED2', power: 'Out 5', sheetRow: 170],
            [id: '4.5', room: 'living', description: 'window inside curtain', led: 'LED3', power: 'Out 9', sheetRow: 171],
            [id: '4.6', room: 'living', description: 'window outside curtain', led: 'LED4', power: 'Out 3', sheetRow: 172],
            [id: '11.4', room: 'master bath', description: 'shower niche', led: 'LED5', power: 'Out 7', sheetRow: 173],
            [id: '11.6', room: 'master bath', description: 'vanity baseboard', led: 'LED6', power: 'Out 11', sheetRow: 174],
        ]],
        [brain: 'brain6', ip: '10.0.0.55', lights: [
            [id: '13.1', room: 'balcony', description: 'balcony west', led: 'LED1', power: 'Out 1', sheetRow: 182],
            [id: '7.3', room: 'guest bath', description: 'niche', led: 'LED2', power: 'Out 5', sheetRow: 183],
            [id: '7.4', room: 'guest bath', description: 'baseboard', led: 'LED3', power: 'Out 9', sheetRow: 184],
            [id: '9.6', room: 'office', description: 'window inside curtain', led: 'LED4', power: 'Out 3', sheetRow: 185],
            [id: '9.7', room: 'office', description: 'window outside curtain', led: 'LED5', power: 'Out 7', sheetRow: 186],
            [id: '10.11', room: 'master', description: 'window outside curtain', led: 'LED6', power: 'Out 11', sheetRow: 187],
            [id: '10.12', room: 'master', description: 'N baseboard', led: 'LED7', power: 'Out 2', sheetRow: 188],
        ]],
        */
        [brain: 'brain7', ip: '10.0.0.56', lights: [
            [id: 'temp1', room: '', description: '', led: 'LED1', power: 'Out 1', sheetRow: 196],
            [id: 'temp2', room: '', description: '', led: 'LED2', power: 'Out 8', sheetRow: 197],
            [id: 'temp3', room: '', description: '', led: 'LED3', power: 'Out 5', sheetRow: 198],
            [id: 'temp4', room: '', description: '', led: 'LED4', power: 'Out 12', sheetRow: 199],
        ]],
    ]
}

private String wledLightId(String segmentName) {
    String text = segmentName?.trim() ?: ""
    // Allow either "10.01" or "10.01 - ceiling", preserving 10.01 vs 10.1.
    def match = (text =~ /^(\d+\.\d{1,2})(?:$|[\s:\-–])/) 
    return match.find() ? match.group(1) : text
}

private List<Map> readSegments(String brain, String ip) {
    Map result = [:]
    httpGet([
        uri: "http://${ip}/json/state",
        contentType: "application/json",
        timeout: 8
    ]) { response ->
        result = (response.data instanceof Map) ?
            (Map)response.data :
            (Map)new JsonSlurper().parseText(response.data.toString())
    }
    if (!(result.seg instanceof List)) {
        throw new IllegalStateException("${brain}: /json/state lacks seg list")
    }
    List<Map> segments = []
    result.seg.each { segment ->
        if (!(segment instanceof Map) || segment.id == null) return
        // Ignore inactive zero-length segments; retain active/off segments.
        if (segment.start != null && segment.stop != null &&
            (segment.stop as Integer) <= (segment.start as Integer)) return
        segments << [id: (segment.id as Integer),
                     name: segment.n?.toString()?.trim() ?: ""]
    }
    return segments
}

private List<Map> buildPlan() {
    List<Map> targets = []
    Set<String> usedDeviceIds = [] as Set
    List<Map> configs = inventory()
    int lightCount = configs.sum { it.lights?.size() ?: 0 } as Integer
    if (lightCount != 4) {
        throw new IllegalStateException("brain7 test inventory must contain exactly 4 lights; found ${lightCount}")
    }
    configs.each { cfg ->
        List<Integer> octets = (cfg.ip?.toString() ?: "").tokenize('.').collect {
            it.isInteger() ? it.toInteger() : -1
        }
        if (octets.size() != 4 || octets.any { it < 0 || it > 255 }) {
            throw new IllegalStateException("${cfg.brain}: invalid IPv4 address '${cfg.ip}'")
        }
        Set<String> brainLightIds = [] as Set
        /* Live WLED discovery and mapping validation disabled for brain7 test.
        List<Map> segments = readSegments(cfg.brain as String, cfg.ip as String)
        // Validate live segment IDs and names against light IDs here.
        */
        cfg.lights.each { light ->
            String lightId = light.id as String  // Name comes from workbook ID column
            String dni = "${lightId}-${cfg.brain}"
            if (!lightId?.trim()) {
                throw new IllegalStateException("${cfg.brain}: light ID must not be blank")
            }
            if (!brainLightIds.add(lightId) || !usedDeviceIds.add(dni)) {
                throw new IllegalStateException("Duplicate Device Network ID: ${dni}")
            }
            def ledMatch = (light.led as String) =~ /^LED([1-9]\d*)$/
            if (!ledMatch.matches()) {
                throw new IllegalStateException("${dni}: invalid LED output '${light.led}'")
            }
            int segmentId = ledMatch[0][1].toInteger() - 1
            targets << [dni: dni, brain: cfg.brain, ip: cfg.ip,
                        lightId: lightId, room: light.room ?: '',
                        description: light.description ?: '', led: light.led,
                        segment: segmentId, mapping: 'workbook LED order']
        }
    }
    return targets
}

void runInstaller() {
    unschedule("installNext")
    state.pending = []
    state.nextIndex = 0
    state.tally = [created: 0, updated: 0, failed: 0]
    List<Map> targets = buildPlan()
    Map counts = targets.countBy { it.brain }
    log.info "Brain Lights Installer: prepared ${targets.size()} lights. By brain: ${counts}"
    targets.each { t ->
        log.info "${t.lightId} | ${t.room} | ${t.description} | ${t.brain}" +
                 " ${t.ip} | ${t.led} -> WLED segment ${t.segment} (${t.mapping})"
    }
    if (settings.operation != "install") {
        state.lastResult = "Preview: ${targets.size()} workbook lights. Inspect Logs."
        return
    }
    if (targets.isEmpty()) {
        state.lastResult = "No devices to create."
        return
    }
    state.pending = targets
    state.lastResult = "Installing ${targets.size()} workbook lights; see Logs."
    runIn(1, "installNext")
}

void installNext() {
    List<Map> pending = state.pending ?: []
    int index = (state.nextIndex ?: 0) as Integer
    if (index >= pending.size()) {
        Map tally = state.tally ?: [:]
        state.lastResult = "Finished. Created ${tally.created ?: 0}, " +
                           "updated ${tally.updated ?: 0}, failed ${tally.failed ?: 0}."
        log.info "Brain Lights Installer: ${state.lastResult}"
        state.pending = []
        return
    }
    Map target = pending[index]
    Map tally = state.tally ?: [created: 0, updated: 0, failed: 0]
    try {
        String deviceName = "${target.lightId}-${target.brain}"
        String label = [target.lightId, target.room, target.description]
            .findAll { it?.toString()?.trim() }.join(" - ")
        def child = getChildDevice(target.dni as String)
        boolean exists = (child != null)
        if (!exists) {
            child = addChildDevice(
                "cb4", "WLED cb4", target.dni as String,
                [name: deviceName, label: label, isComponent: false]
            )
        } else {
            child.setName(deviceName)
            if (settings.updateExistingLabels == true) child.setLabel(label)
        }
        child.updateSetting("uri", [value: "http://${target.ip}", type: "text"])
        child.updateSetting("ledSegment", [value: "${target.segment}", type: "number"])
        child.updateSetting("transitionTime", [value: "700", type: "enum"])
        child.updateSetting("refreshInterval", [value: "300", type: "enum"])
        child.updateSetting("powerOffParent", [value: "false", type: "bool"])
        // WLED cb4 retries each failed command at most 3 times.
        // Disable its independent 30-second monitor, which would keep probing
        // an offline controller indefinitely after those retries are exhausted.
        child.updateSetting("enableRetry", [value: "true", type: "bool"])
        child.updateSetting("connectionMonitoring", [value: "false", type: "bool"])
        child.updateSetting("logEnable", [value: "false", type: "bool"])
        child.updated() // Apply preferences, configure polling and refresh state.
        String action = exists ? "updated" : "created"
        tally[action] = (tally[action] ?: 0) + 1
        log.info "${action}: ${label} | ${target.brain} ${target.ip}" +
                 " | ${target.led} | segment ${target.segment}"
    } catch (Exception error) {
        tally.failed = (tally.failed ?: 0) + 1
        log.error "FAILED ${target.lightId} (${target.brain}): ${error.message}"
    }
    state.tally = tally
    state.nextIndex = index + 1
    runIn(8, "installNext")
}
