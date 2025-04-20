# k8s/localstack/Dockerfile
FROM amazon/aws-cli:2.15.36

RUN yum install -y jq

WORKDIR /app

COPY setup.sh /app/setup.sh
COPY render-policy.sh /app/utils/render-policy.sh
COPY policy.json /app/config/policy.json

RUN chmod +x /app/setup.sh /app/utils/render-policy.sh

ENTRYPOINT ["/bin/sh", "-c"]
CMD ["/app/setup.sh"]
