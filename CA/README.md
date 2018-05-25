# Cluster Autoscaling

## Pasos previos:

1. Hay que editar el IAM Role de los nodos para que la instancia que ejecuta el cluster-autoscaling pueda operar con el Autoscaling de AWS. Esto es, añadir este rol:

```
{
  "Sid": "kopsK8sAutoScaling",
  "Effect": "Allow",
  "Action": [
    "autoscaling:DescribeAutoScalingGroups",
    "autoscaling:DescribeAutoScalingInstances",
    "autoscaling:SetDesiredCapacity",
    "autoscaling:TerminateInstanceInAutoScalingGroup"
  ],
  "Resource": [
    "*"
  ]
}
```

2. Editar el AutoScaling group en AWS para aumentar el **máximo de instancias** que podemos levantar.

3. Editar los párametros del comando `run.sh`, y ejecutarlo.

4. Para probarlo podemos hacer uso del mismo test que en HPA, veremos que llega un momento que los pods quedan _pending_ porque no hay CPU para todos y el cluster escala a nivel de nodos.