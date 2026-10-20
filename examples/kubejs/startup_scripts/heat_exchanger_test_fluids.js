

StartupEvents.registry('fluid', event => {
  event.create('immcheme_test_superheated')
    .displayName('Superheated Test Fluid')
    .thickTexture(0xff3b18)
    .temperature(1200)
    .luminosity(8)
    .density(1800)
    .viscosity(2200)

  event.create('immcheme_test_warm')
    .displayName('Warm Test Fluid')
    .thickTexture(0xffa52b)
    .temperature(700)
    .density(1300)
    .viscosity(1500)

  event.create('immcheme_test_chilled')
    .displayName('Chilled Test Fluid')
    .thinTexture(0x1f70ff)
    .temperature(250)
    .density(1000)
    .viscosity(900)

  event.create('immcheme_test_tempered')
    .displayName('Tempered Test Fluid')
    .thinTexture(0x42d4c7)
    .temperature(500)
    .density(1100)
    .viscosity(1100)
})
