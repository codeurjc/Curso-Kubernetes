Tareas
===

# Desplegar ejemplos del tema de Docker en Kubernetes

## Aplicación web python (un contenedor) (slide 78)

Creado el YAML con la definición del deployment y service en webgatos.yaml.

Es una aplicación bastante simple que mete en un contenedor docker un código Python y lo ejecuta.


## Aplicación web python con BBDD Mongo (dos contenedores, docker-compose)

- En los ficheros mongo.yaml y python-mongo.yaml hemos definido una aplicación Python que se conecta a una base de datos Mongo. El Mongo tarda en arrancar y da timeout desde el front a la conexión de la BBDD.

## Volumen de MongoDB

## Escalado de la web en kubernetes (replicas del pod)



# Cómo desplegar Kubernetes en AWS

## Formatos: CloudFormation (heptio), EKS, otros?

- kops

Para desplegar Kubernetes en AWS utilizamos la herramienta Kops. Es la oficial de Kubernetes para el despliegue, esperemos que el lanzamiento de EKS no afecte al funcionamiento.

Para el despliegue seguimos la guía:

https://github.com/kubernetes/kops/blob/master/docs/aws.md

Primero hemos creado el IAM con los permisos adecuados para operar el cluster.

Hemos utilizado el escenario 3, ya que tenemos el dominio en un proveedor externo y hemos creado un subdominio especifico para Kubernetes.

Exportamos la variables de entorno necesarias para el despliegue:

```
export NAME=cluster.k8s.codeurjc.es
export KOPS_STATE_STORE=s3://kubernetes-state-codeurjc
export AWS_PROFILE=kops
```

Tenemos la opción de desplegar en un VPC expuesta, lo único que evitará que los servicios estén abiertos será el grupo de seguridad:

```
kops create cluster \
  --associate-public-ip=true \
  --cloud=aws \
  --bastion=false \
  --dns-zone=k8s.codeurjc.es \
  --master-count=1 \
  --master-size=t2.medium \
  --master-volume-size=60 \
  --master-zones=eu-west-1a \
  --network-cidr=10.230.0.0/16 \
  --networking=calico \
  --node-count=3 \
  --node-size=t2.large \
  --node-volume-size=12 \
  --target=direct \
  --topology=public \
  --zones=eu-west-1a,eu-west-1b,eu-west-1c \
  --ssh-public-key=~/.ssh/id_rsa.pub \
  --name=$NAME \
  --state=$KOPS_STATE_STORE \
  --yes
```

O en un red aislada de internet por lo que el acceso será a traves de un bastión SSH.

```
kops create cluster \
  --associate-public-ip=false \
  --cloud=aws \
  --bastion=true \
  --dns-zone=k8s.codeurjc.es \
  --master-count=1 \
  --master-size=t2.2xlarge \
  --master-volume-size=60 \
  --master-zones=eu-west-1a \
  --network-cidr=10.230.0.0/16 \
  --networking=calico \
  --node-count=3 \
  --node-size=t2.2xlarge \
  --node-volume-size=20 \
  --target=direct \
  --topology=private \
  --zones=eu-west-1a \
  --ssh-public-key=~/.ssh/id_rsa.pub \
  --name=$NAME \
  --state=$KOPS_STATE_STORE \
  --yes
```

El despliegue en ambos casos tarda unos minutos, cuando esté listo veremos que la verificación es OK:

`kops validate cluster $NAME`

Ya podemos empezar a operar nuestro cluster con `kubectl`

`kubectl get nodes`

Para eliminar el cluster:

` kops delete cluster $NAME --yes`

- heptio

Heptio es proporcionado por AWS para desplegar Kubernetes usando CloudFormation.

Para desplegarlos consultamos la documentación aquí:

https://s3.amazonaws.com/quickstart-reference/heptio/latest/doc/heptio-kubernetes-on-the-aws-cloud.pdf

Podemos usar el siguiente comando para desplegarlo desde la consola:

En una VPC que ya exite:

```
aws cloudformation create-stack \
  --stack-name heption-kubernetes \
  --template-url https://s3-eu-west-1.amazonaws.com/heptio-k8s/Heptio-CF.yaml \
  --parameters '[{"ParameterKey":"VPCID","ParameterValue":"vpc-6bf6a10f"}, {"ParameterKey":"AvailabilityZone","ParameterValue":"eu-west-1a"}, {"ParameterKey":"InstanceType","ParameterValue":"t2.small"}, {"ParameterKey":"DiskSizeGb","ParameterValue":"40"}, {"ParameterKey":"ClusterSubnetId","ParameterValue":"subnet-f1ed81a9"}, {"ParameterKey":"LoadBalancerSubnetId","ParameterValue":"subnet-f1ed81a9"}, {"ParameterKey":"LoadBalancerType","ParameterValue":"internet-facing"}, {"ParameterKey":"SSHLocation","ParameterValue":"0.0.0.0/0"}, {"ParameterKey":"ApiLbLocation","ParameterValue":"0.0.0.0/0"}, {"ParameterKey":"KeyName","ParameterValue":"nordri-aws-urjc"}, {"ParameterKey":"K8sNodeCapacity","ParameterValue":"3"}, {"ParameterKey":"NetworkingProvider","ParameterValue":"calico"}, {"ParameterKey":"ClusterDNSProvider","ParameterValue":"CoreDNS"}, {"ParameterKey":"QSS3BucketName","ParameterValue":"aws-quickstart"}, {"ParameterKey":"QSS3KeyPrefix","ParameterValue":"quickstart-heptio/"},{"ParameterKey":"ClusterAssociation", "ParameterValue":"urjc"} ]' \
  --capabilities CAPABILITY_IAM CAPABILITY_NAMED_IAM \
  --profile naeva
```

Crear una VPC específica para Kubernetes:

```
aws cloudformation create-stack \
  --stack-name heption-kubernetes \
  --template-url https://s3-eu-west-1.amazonaws.com/heptio-k8s/Heptio-CF-NewVPC.yaml \
  --parameters '[{"ParameterKey":"AvailabilityZone","ParameterValue":"eu-west-1a"}, {"ParameterKey":"AdminIngressLocation","ParameterValue":"0.0.0.0/0"}, {"ParameterKey":"KeyName","ParameterValue":"nordri-aws-urjc"}, {"ParameterKey":"ClusterDNSProvider","ParameterValue":"CoreDNS"}, {"ParameterKey":"NetworkingProvider","ParameterValue":"calico"}, {"ParameterKey":"K8sNodeCapacity","ParameterValue":"3"}, {"ParameterKey":"InstanceType","ParameterValue":"m4.large"}, {"ParameterKey":"DiskSizeGb","ParameterValue":"40"}, {"ParameterKey":"BastionInstanceType","ParameterValue":"t2.micro"}, {"ParameterKey":"QSS3BucketName","ParameterValue":"aws-quickstart"}, {"ParameterKey":"QSS3KeyPrefix","ParameterValue":"quickstart-heptio/"} ]' \
  --capabilities CAPABILITY_IAM CAPABILITY_NAMED_IAM \
  --profile naeva
```

- kubeadm

## Kubernetes en Cloud

### Load Balancer es compartido entre todos los servicios

### Volumenes > EBS

### Otro integración? 

### Autoscaling de AWS con workers Kubernetes

* scale-in

* scale-out

El cluster crece y decrece usando las herramientas de autoescalado de AWS. Al decrecer vuelve a recrear los contenedores en los nodos que queden disponibles. Hay interrupción del servicio el tiempo que tarda kubernetes en darse cuenta que ha perdido el nodo (15 - 20 segundos en un cluster de 3 nodos)

Avisar de decrecimiento? -> 

Con kubectl drain $NODE se etiqueta el node como en mantenimiento y automáticamente pasa todos los contenedores a un nodo disponible.

Con kubectl uncordon $NODE el nodo vuelve a estar disponible pero el Scheduler no devuelve los contenedores a su nodo, es decir, el nodo está disponible y vacio.

Hay un kubectl cordon $NODE que marca el nodo como en mantenimiento pero no lo evacua.

# Desplegar apps “cloud-native”

## JHipster

* Versión monolítica en Kubernetes

* Versión microservicios en Kubernetes

* Demo de https://github.com/codeurjc/SquirrelDrey (limitado porque tenemos que saber cómo configurar hazelcast para Kubernetes).

- https://github.com/hazelcast/hazelcast-kubernetes
- https://hub.docker.com/r/hazelcast/hazelcast-kubernetes/
- https://github.com/hazelcast/hazelcast-docker/blob/master/hazelcast-kubernetes/README.md

He trabajado con la sample app de Pablo para Squirrel Drey y esta documentado aquí:                                 

https://github.com/codeurjc/SquirrelDrey/tree/master/squirrel-drey-kubernetes

* FullTeaching

# Misc

## Logging

- Centralizar los logs de los pods usando Fluentd + EKL

## Auth

- Nos podemos autenticar frente al API del kubernetes con certificados X509 que es por defecto o

- Basic auth
- Proxy
- OpenID

## Authorization

- Al crear el cluster con kops es recomendable pasar el flag --authorization para que configure los roles de los elementos.

- Si no se ha hecho, añadir el flag --authorization-mode=RBAC al api server

- RBAC

	- Crear roles y asignar a los usuarios o grupos
	- El permisos puede ser para un namespace espcifico o para todos

## Ingress

Para desplegar Ingress seguimos la guía:

https://github.com/kubernetes/ingress-nginx/blob/master/docs/deploy/index.md

Si estamos usando RBAC

