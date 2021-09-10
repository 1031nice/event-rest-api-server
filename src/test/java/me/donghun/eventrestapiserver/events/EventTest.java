package me.donghun.eventrestapiserver.events;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EventTest {

    @Test
    void free() {
        Event freeEvent = Event.builder()
                .basePrice(0)
                .maxPrice(0)
                .build();

        freeEvent.update();

        assertThat(freeEvent.isFree()).isTrue();

        Event maxPriceZeroEvent = Event.builder()
                .basePrice(1)
                .maxPrice(0)
                .build();

        maxPriceZeroEvent.update();

        assertThat(maxPriceZeroEvent.isFree()).isFalse();

        Event basePriceZeroEvent = Event.builder()
                .basePrice(0)
                .maxPrice(1)
                .build();

        basePriceZeroEvent.update();

        assertThat(basePriceZeroEvent.isFree()).isFalse();
    }
    
    @Test
    void offline() {
        Event offlineEvent = Event.builder()
                .location("somewhere")
                .build();

        offlineEvent.update();

        assertThat(offlineEvent.isOffline()).isTrue();

        Event onlineEvent = Event.builder()
                .build();

        onlineEvent.update();

        assertThat(onlineEvent.isOffline()).isFalse();
    }

}
