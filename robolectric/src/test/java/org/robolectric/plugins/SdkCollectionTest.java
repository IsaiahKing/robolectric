package org.robolectric.plugins;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;
import org.robolectric.pluginapi.Sdk;
import org.robolectric.pluginapi.SdkProvider;

public class SdkCollectionTest {

  private SdkProvider mockSdkProvider;
  private SdkCollection sdkCollection;
  private Sdk fakeSdk1234;
  private Sdk fakeSdk1235;
  private Sdk fakeSdk1236;
  private Sdk fakeUnsupportedSdk1237;

  @Before
  public void setUp() throws Exception {
    mockSdkProvider = mock(SdkProvider.class);
    fakeSdk1234 = new StubSdk(1234, true);
    fakeSdk1235 = new StubSdk(1235, true);
    fakeSdk1236 = new StubSdk(1236, true);
    fakeUnsupportedSdk1237 = new StubSdk(1237, false);
    when(mockSdkProvider.getSdks())
        .thenReturn(Arrays.asList(fakeSdk1235, fakeSdk1234, fakeSdk1236, fakeUnsupportedSdk1237));

    sdkCollection = new SdkCollection(mockSdkProvider);
  }

  @Test
  public void shouldComplainAboutDupes() throws Exception {
    try {
      new SdkCollection(() -> Arrays.asList(fakeSdk1234, fakeSdk1234));
      fail();
    } catch (Exception e) {
      assertThat(e.getMessage()).contains("duplicate SDKs for API level 1234");
    }
  }

  @Test
  public void shouldCacheSdks() throws Exception {
    assertThat(sdkCollection.getSdk(1234)).isSameAs(fakeSdk1234);
    assertThat(sdkCollection.getSdk(1234)).isSameAs(fakeSdk1234);

    verify(mockSdkProvider, times(1)).getSdks();
  }

  @Test
  public void getMaxSupportedSdk() throws Exception {
    assertThat(sdkCollection.getMaxSupportedSdk()).isSameAs(fakeSdk1236);
  }

  @Test
  public void getSdk_shouldReturnNullObjectForUnknownSdks() throws Exception {
    assertThat(sdkCollection.getSdk(4321)).isNotNull();
    assertThat(sdkCollection.getSdk(4321).isKnown()).isFalse();
  }

  @Test
  public void getSupportedSdks_shouldReturnOnlySupported() throws Exception {
    assertThat(sdkCollection.getSupportedSdks())
        .containsExactly(fakeSdk1234, fakeSdk1235, fakeSdk1236).inOrder();
  }

  private static class StubSdk extends Sdk {

    private final boolean isSupported;

    public StubSdk(int apiLevel, boolean isSupported) {
      super(apiLevel);
      this.isSupported = isSupported;
    }

    @Override
    public String getAndroidVersion() {
      return null;
    }

    @Override
    public String getAndroidCodeName() {
      return null;
    }

    @Override
    public Path getJarPath() {
      return null;
    }

    @Override
    public boolean isSupported() {
      return isSupported;
    }

    @Override
    public String getUnsupportedMessage() {
      return null;
    }

    @Override
    public boolean isKnown() {
      return false;
    }
  }
}