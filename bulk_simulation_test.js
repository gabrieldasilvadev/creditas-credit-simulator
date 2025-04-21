import http from 'k6/http';
import { check, sleep } from 'k6';
import { uuidv4 } from 'https://jslib.k6.io/k6-utils/1.4.0/index.js';

// Parametrização via ENV com valores padrão
export let options = {
  vus: parseInt(__ENV.VUS || '1', 10),
  iterations: parseInt(__ENV.ITERATIONS || '1', 10),
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:7000';
const TOTAL_SIMULATIONS = parseInt(__ENV.SIMULATIONS || '1000', 10);

function randomAmount() {
  return (Math.random() * 100000).toFixed(2);
}

function randomBirthDate() {
  const year = Math.floor(Math.random() * (2004 - 1950 + 1)) + 1950;
  const month = String(Math.floor(Math.random() * 12) + 1).padStart(2, '0');
  const day = String(Math.floor(Math.random() * 28) + 1).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

function randomCurrency() {
  return Math.random() < 0.5 ? 'BRL' : 'USD';
}

function randomPolicy() {
  const policies = ['fixed', 'age_based'];
  return policies[Math.floor(Math.random() * policies.length)];
}

function randomEmail(index) {
  return `cliente${index}-${uuidv4().substring(0, 4)}@example.com`;
}

function generateSimulations(count) {
  const sims = [];
  for (let i = 0; i < count; i++) {
    sims.push({
      loan_amount: {
        amount: randomAmount(),
        currency: randomCurrency(),
      },
      customer_info: {
        birth_date: randomBirthDate(),
        email: randomEmail(i),
      },
      months: Math.floor(Math.random() * 60) + 6,
      policy_type: randomPolicy(),
      source_currency: 'BRL',
      target_currency: 'USD',
    });
  }
  return sims;
}

export default function () {
  const payload = JSON.stringify({
    simulations: generateSimulations(TOTAL_SIMULATIONS),
  });

  const headers = { 'Content-Type': 'application/json' };

  const url = `${BASE_URL}/simulations/bulk`;

  const res = http.post(url, payload, { headers });

  check(res, {
    'status is 200': (r) => r.status === 200,
  });

  console.log(`Sent ${TOTAL_SIMULATIONS} simulations → status ${res.status}`);
  sleep(1);
}
