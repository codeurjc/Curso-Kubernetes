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

