# Ingress Controller

Para instalar el Ingress Controller seguimos esta guía

https://kubernetes.github.io/ingress-nginx/deploy/

Y luego desplegamos la aplicación _ejemplo1/webgatos-aws-ingress.yaml_

Vemos la dirección del balanceador

	$ kubectl describe webgatos-service

Y la registramos en Route53 como CNAME con un TTL de 60 segundos.