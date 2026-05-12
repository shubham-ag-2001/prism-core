package com.prism.core.provider.device.service;

import com.prism.core.common.enums.ProviderType;
import com.prism.core.provider.device.dto.DeviceInfoRequest;
import com.prism.core.provider.device.dto.DeviceInfoResponse;
import com.prism.core.provider.entity.RawSignal;
import com.prism.core.provider.repository.RawSignalRepository;
import com.prism.core.user.entity.User;
import com.prism.core.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Internal service that derives device-integrity risk flags (D5/RSK05) from
 * the app list and device metadata submitted by the Android app.
 *
 * All derivation logic lives here — no 3rd-party call needed.
 * Results are persisted as RawSignal rows with provider_type=DEVICE.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceSignalProcessorService {

    private final RawSignalRepository rawSignalRepository;
    private final UserRepository      userRepository;

    // ── Known-bad app blocklists ──────────────────────────────────────────────

    /** Apps that fake GPS / spoof location */
    private static final Set<String> LOCATION_SPOOF_APPS = Set.of(
            "com.lexa.fakegps",
            "com.incorporateapps.fakegps.fre",
            "com.hola.fake.gps.location",
            "ru.gavrikov.mocklocations",
            "com.byterev.mockgpspro",
            "com.blogspot.newapphorizons.fakegps"
    );

    /** Apps used to fabricate screenshots / tamper with records */
    private static final Set<String> FABRICATION_APPS = Set.of(
            "de.robv.android.xposed.installer",
            "com.topjohnwu.magisk",
            "org.lsposed.manager",
            "com.lucky.draw.ludo",
            "io.va.exposed"
    );

    /** Known apps from high-risk gambling / fantasy sports platforms */
    private static final Set<String> CREDIT_HUNGRY_APPS = Set.of(
            "com.dream11",
            "com.myfab11",
            "com.myteam11.fantasy",
            "com.sportzbet.fantasy",
            "in.redball.rummy"
    );

    // ── Main Process Method ───────────────────────────────────────────────────

    @Transactional
    public DeviceInfoResponse process(UUID userId, DeviceInfoRequest request) {
        log.info("[DeviceSignal] Processing device data for user={}", userId);

        DeviceInfoRequest.DeviceMetadata meta = request.getDeviceMetadata();
        List<String> appList = request.getAppList() != null ? request.getAppList() : List.of();

        // ── Derive flags ──────────────────────────────────────────────────────
        boolean isRooted             = meta.isRooted();
        boolean isDeveloperMode      = meta.isDeveloperModeEnabled();
        boolean isEmulator           = meta.isEmulator();
        boolean hasLocationSpoof     = appList.stream().anyMatch(LOCATION_SPOOF_APPS::contains);
        boolean hasFabricationTools  = appList.stream().anyMatch(FABRICATION_APPS::contains);
        // Unofficial APKs heuristic: rooted + any unknown source (if Magisk or Xposed present)
        boolean hasUnofficialApks    = hasFabricationTools || isRooted;
        // Credit-hungry score: proportion of credit/lending/gambling apps installed (0-100)
        long creditRiskyCount        = appList.stream().filter(CREDIT_HUNGRY_APPS::contains).count();
        double creditHungryScore     = Math.min(100.0, creditRiskyCount * 20.0);

        // ── Persist signals ───────────────────────────────────────────────────
        User user = userRepository.getReferenceById(userId);
        List<RawSignal> signals = new ArrayList<>();

        signals.add(signal(user, "is_device_rooted",              String.valueOf(isRooted)));
        signals.add(signal(user, "is_developer_mode_on",          String.valueOf(isDeveloperMode)));
        signals.add(signal(user, "is_emulator",                   String.valueOf(isEmulator)));
        signals.add(signal(user, "has_location_spoof_app",        String.valueOf(hasLocationSpoof)));
        signals.add(signal(user, "has_record_fabrication_tools",  String.valueOf(hasFabricationTools)));
        signals.add(signal(user, "has_unofficial_apks",           String.valueOf(hasUnofficialApks)));
        signals.add(signal(user, "credit_hungry_score",           String.valueOf(creditHungryScore)));

        rawSignalRepository.saveAll(signals);
        log.info("[DeviceSignal] Persisted {} device signals for user={}", signals.size(), userId);

        return DeviceInfoResponse.builder()
                .processedSuccessfully(true)
                .signalsExtracted(signals.size())
                .isRooted(isRooted)
                .isDeveloperModeEnabled(isDeveloperMode)
                .isEmulator(isEmulator)
                .hasLocationSpoofApp(hasLocationSpoof)
                .hasRecordFabricationTools(hasFabricationTools)
                .hasUnofficialApks(hasUnofficialApks)
                .creditHungryScore(creditHungryScore)
                .message("Device signals processed and stored (" + signals.size() + " signals)")
                .build();
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private RawSignal signal(User user, String key, String value) {
        return RawSignal.builder()
                .user(user)
                .providerType(ProviderType.DEVICE)
                .signalKey(key)
                .signalValue(value)
                .build();
    }
}
