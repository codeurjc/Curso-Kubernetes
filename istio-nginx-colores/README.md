# Jugando con nginx

Dos versiones: green y red.

## Despliegue normal:

`$ cd /home/nordri/sandbox/Kubernetes/istio/istio-nordri-test/nginx`

Desplegar la aplicacion:

`$ kubectl create -f nginx-color.yaml`

Desplegar el Gateway para conectar el mundo real con la aplicación

`kubectl create -f nginx-color-gateway.yaml`

Desplegar el VirtualService para redirigir el tráfico desde el exterior a la aplicación.

`kubectl create -f nginx-color-vs-all.yaml`

Obtenemos la URL de acceso

```
$ export INGRESS_HOST=$(kubectl -n istio-system get service istio-ingressgateway -o jsonpath='{.status.loadBalancer.ingress[0].ip}')
$ export INGRESS_PORT=$(kubectl -n istio-system get service istio-ingressgateway -o jsonpath='{.spec.ports[?(@.name=="http2")].port}')

# Si estamos en AWS usamos el hostname
export INGRESS_HOST=$(kubectl -n istio-system get service istio-ingressgateway -o jsonpath='{.status.loadBalancer.ingress[0].hostname}')

$ echo http://$INGRESS_HOST:$INGRESS_PORT/app # este es por si quieres copiarlo a un browser

$ curl -I http://$INGRESS_HOST:$INGRESS_PORT/app # este es la prueba que debe devolver 200
```

## Objetivo uno:

Desplegar las dos versiones y fijar el tráfico a una sola de ellas.

Si hemos desplegado la app simplemente, eliminamos el VirtualService:

`$ kubectl delete -f nginx-color-vs-all.yaml`

Creamos el juego de rutas

`$ kubectl create -f nginx-color-routeConfiguration.yaml`

Fijar la versión roja:

`$ kubectl create -f nginx-color-fixed-red.yaml`

Fijar la versión verde:

`$ kubectl create -f nginx-color-fixed-green.yaml`

## Objetivo dos:

Usar una versión mucho más que otra, un 90/10 en este caso:

Asegurarse de que no existe ningún VirtualService previo.

`$ kubectl create -f nginx-color-percentage-of-use.yaml`

Para comprobarlo en lugar de usar un browser que cachea la sesión y puede darnos falsos resultados usaremos este pequeño script:

```
./statics.sh
```

## Objetivo tres - uno:

Redirigir tráfico en función de la cabecera HTTP y añadir una latencia de red

`$ kubectl create -f nginx-color-delay.yaml`

En el browser podemos ver como siguen funcionando las dos versiones pero si colocamos una cabecera que coincida con nuestras reglas veremos como se introduce una latencia

`$ time { curl -IL -H "x-version: red" --connect-timeout 30  http://$INGRESS_HOST:$INGRESS_PORT/app ; }`

```
HTTP/1.1 503 Service Unavailable
date: Sat, 02 Feb 2019 08:35:31 GMT
server: envoy
transfer-encoding: chunked


real	0m20.132s
user	0m0.004s
sys	0m0.004s
```

Si cambiamos la cabecera:

`$ time { curl -IL -H "x-version: green" --connect-timeout 30  http://$INGRESS_HOST:$INGRESS_PORT/app ; }`

```
HTTP/1.1 301 Moved Permanently
server: envoy
date: Fri, 08 Mar 2019 12:48:32 GMT
content-type: text/html
content-length: 186
location: http://35.202.249.101/app/
x-envoy-upstream-service-time: 5

HTTP/1.1 200 OK
server: envoy
date: Fri, 08 Mar 2019 12:48:52 GMT
content-type: text/html
content-length: 361
last-modified: Fri, 08 Mar 2019 12:11:23 GMT
etag: "5c825beb-169"
accept-ranges: bytes
x-envoy-upstream-service-time: 2


real	0m40.505s
user	0m0.004s
sys	0m0.004s
```

## Objetivo tres - dos:

Error HTTP 400 (o cualquiera) en función de la cabezera http

`$ kubectl create -f nginx-color-http-error.yaml`

## Objetivo tres - tres:

Error HTTP 400 (o cualquiera) y un delay en función de la cabezera http

`$ kubectl create -f nginx-color-http-error-and-delay.yaml`

## Objetivo cinco:

Circuit breaker. Introducir fallos en la red mediante limitación de la capacidad.

Referencia: https://istio.io/docs/tasks/traffic-management/circuit-breaking/

`$ cd /home/nordri/sandbox/Kubernetes/istio/istio-nordri-test/circuit-breaker`

kubectl create -f httpbin.yaml

`$ kubectl apply -f httpbin-destinationrule.yaml`
