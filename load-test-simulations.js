import http from 'k6/http';
import { check, sleep } from 'k6';

export let options = {
  vus: 100,
  iterations: 1000000,
};

export default function () {
  const url = 'http://localhost:7000/simulations';

  const payload = JSON.stringify({
    loan_amount: {
      amount: "10000.00",
      currency: "BRL"
    },
    customer_info: {
      birth_date: "1990-01-01",
      email: "cliente@teste.com"
    },
    months: 12,
    source_currency: "BRL",
    target_currency: "USD"
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
