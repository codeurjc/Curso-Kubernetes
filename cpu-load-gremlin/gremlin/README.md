# Gremlin - Chaos as a Service

Es una web que permite introducir caos en nuestro cluster.

https://www.gremlin.com/community/tutorials/how-to-install-and-use-gremlin-with-kubernetes/

En esa página te explica los primeros pasos. Primero debes crear una cuenta. Cuando la tengas descargas los certificados y los importas como secretos a kubernetes.

$ kubectl create secret generic gremlin-team-cert --from-file=./gremlin.crt --from-file=./gremlin.key

Para desplegar el demonio de Gremlin hacemos uso del spec gremlin-daemonSet.yaml que hemos obtenido de aquí:

https://www.gremlin.com/docs/infrastructure-layer/installation/#installation-with-kubectl

Hemos tenido que sustituir la parte de las variables, el team id y las claves públicas.

Una vez hemos creado el daemon podemos ver en la interface web (Clients -> Infraestructure) los nodos que conforman nuestro cluster.

Ahora podemos ir a Attack y programar un ataque, el más efectivo dentro del plan free es el de carga de cpu, podemos lanzarlo sobre un solo nodo o varios por un tiempo de hasta 1 hora, el demonio en nuestra infra recibe la petición y aumenta la carga de CPU del nodo/s. Podemos ver como aumenta en las metricas de GKE.


# Sin chaos:

Hacemos 5 peticiones seguidas en intervalos de 2 segundos

```
for i in $(seq 1 5); do 
  curl -w "@curl-format.txt" -o /dev/null -s "http://34.76.78.47/"; 
  sleep 2s;
done
```

time_total:  0,806
time_total:  0,586
time_total:  0,860
time_total:  0,714
time_total:  0,601

# Con chaos

time_total:  0,987
time_total:  0,991
time_total:  0,997
time_total:  0,980
time_total:  1,038

Es dificil ver bien el chaos porque a pesar de ser una máquina pequeña y ver que efectivamente hay un proceso al 9x% el script de PHP se ejecuta en un tiempo más que razonable. He tirado varias pruebas y no he conseguido disparar los tiempos de ejecución.