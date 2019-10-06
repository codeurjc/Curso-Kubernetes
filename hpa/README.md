# Horizontal Pod Autoscaler

Después de desplegar Kubernetes con KOPS

1. Añadimos el Kops Addon para recolectar las métricas.

	$ kubectl apply -f https://raw.githubusercontent.com/kubernetes/kops/master/addons/metrics-server/v1.8.x.yaml

2. Para probar podemos lanzar un deployment que haga un uso intensivo de la CPU

- Desplegar deployment.yaml

	$ kubectl create -f deployment.yaml

- Crear el recurso de escalado horizontal

	$ kubectl autoscale deployment deploy-cpu-test --min=1 --max=10 --cpu-percent=50

- Supervisar el escalado:

	$ watch kubectl get pods --selector app=cpu-test

## Minikube

Para probar en Minikube seguimos estos pasos:

Hay que comprobar en minikube que los addons de métricas están habilitados:

```
$ minikube addons enable heapster
$ minikube addons enable metrics-server
```

Creamos un Deployment consistente en una pequeña aplicación PHP que con cada petición genera carga de CPU

`$ kubectl create -f deployment.yaml`

Creamos un Horizontal Autoscaling que supervisará tanto la memoria como la CPU y escalará los pods entre 1 y 10

`$ kubectl create -f hpa-autoscaling.yaml `

Ahora lanzamos un pod desde el que vamos a lanzar carga a la aplicación PHP

`$ kubectl run --generator=run-pod/v1 -i --tty load-generator --image=busybox /bin/sh`

Dentro del contenedor:

`$ while true; do wget -q -O- http://php-apache.default.svc.cluster.local; done`

Podemos observar como escalan los pods:

`$ watch kubectl get pods –selector=app=php-apache`

También podemos ver en Grafana como se incrementa la carga al hacer las pruebas. Para ello nos conectamos al servicio:

`$ minikube addons open heapster`

Y nos situamos en el _Dashboard_ **PODS** y buscamos nuestro pod original. Veremos que está ocioso y podremos comprobar como sube la carga conforme hacemos la prueba.

Referencias: 
- https://kubernetes.io/docs/tasks/run-application/horizontal-pod-autoscale-walkthrough/
- https://www.digitalocean.com/community/tutorials/how-to-autoscale-your-workloads-on-digitalocean-kubernetes
