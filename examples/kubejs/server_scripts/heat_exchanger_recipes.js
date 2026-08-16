

ServerEvents.recipes(event => {
  
  event.recipes.immersivechemical.heat_exchanger(
    'kubejs:icheme_test_superheated', 1000,
    'kubejs:icheme_test_warm', 1000,
    400,
    'cool_down'
  ).id('kubejs:immersivechemical/test_cool_superheated')

  
  event.recipes.immersivechemical.heat_exchanger(
    'kubejs:icheme_test_chilled', 500,
    'kubejs:icheme_test_tempered', 500,
    200,
    'heat_up'
  ).id('kubejs:immersivechemical/test_heat_chilled')
})
