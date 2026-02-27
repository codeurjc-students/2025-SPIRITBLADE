'use strict';

// Genera un usuario único por virtual user para evitar colisiones en BD
function generateUser(context, events, done) {
  const id = `${Date.now()}_${Math.random().toString(36).slice(2, 7)}`;
  context.vars['username'] = `loadtest_${id}`;
  context.vars['email']    = `loadtest_${id}@spiritblade.test`;
  context.vars['password'] = 'LoadTest2024#';
  return done();
}

module.exports = { generateUser };
