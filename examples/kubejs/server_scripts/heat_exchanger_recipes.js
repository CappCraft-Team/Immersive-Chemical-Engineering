// Copy this file to kubejs/server_scripts in a pack or server instance.
// The matching fluids are registered by startup_scripts/heat_exchanger_test_fluids.js.
ServerEvents.recipes(event => {
  // 1000 mB superheated -> 1000 mB warm releases 400 heat.
  event.recipes.immersivechemical.heat_exchanger(
    'kubejs:icheme_test_superheated', 1000,
    'kubejs:icheme_test_warm', 1000,
    400,
    'cool_down'
  ).id('kubejs:immersivechemical/test_cool_superheated')

  // Two 500 mB batches chilled -> tempered consume 2 x 200 heat.
  event.recipes.immersivechemical.heat_exchanger(
    'kubejs:icheme_test_chilled', 500,
    'kubejs:icheme_test_tempered', 500,
    200,
    'heat_up'
  ).id('kubejs:immersivechemical/test_heat_chilled')
})
