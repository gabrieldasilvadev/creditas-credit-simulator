import http from 'k6/http';
import { check, sleep } from 'k6';
import { uuidv4 } from 'https://jslib.k6.io/k6-utils/1.4.0/index.js';

export let options = {
  vus: 100,
  iterations: 1000000,
};

function randomBirthDate() {
  const year = Math.floor(Math.random() * (2004 - 1950 + 1)) + 1950;
  const month = String(Math.floor(Math.random() * 12) + 1).padStart(2, '0');
  const day = String(Math.floor(Math.random() * 28) + 1).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

function randomEmail() {
  return `cliente.${uuidv4().substring(0, 8)}@teste.com`;
}

export default function () {
  const url = 'http://localhost:7000/simulations';

  const payload = JSON.stringify({
    loan_amount: {
      amount: (Math.random() * 100000).toFixed(2),
      currency: "BRL",
    },
    customer_info: {
      birth_date: randomBirthDate(),
      email: randomEmail(),
    },
    months: Math.floor(Math.random() * 48) + 1,
    source_currency: "BRL",
    target_currency: "USD",
  });

  const params = {
    headers: {
      'Content-Type': 'application/json',
    },
  };

  const res = http.post(url, payload, params);

  check(res, {
    'status is 200': (r) => r.status === 200,
  });

  sleep(0.01);
}
