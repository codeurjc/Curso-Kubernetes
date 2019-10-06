#!/bin/bash

function test() {

    output=$(curl --max-time 1 -s $2 )
    if [[ $output == *$3* ]]
    then
        echo $1": OK"
    else
        echo $1:" FAIL"
    fi
}


HOST=$(minikube ip)
SA_PORT=$(kubectl get service servicea-service --output='jsonpath={.spec.ports[0].nodePort}')
SB_PORT=$(kubectl get service serviceb-service --output='jsonpath={.spec.ports[0].nodePort}')

test "ServiceA External Ingress" "http://$HOST:$SA_PORT/internalvalue" "{ value: 0 }"
test "ServiceB External Ingress" "http://$HOST:$SB_PORT/internalvalue" "{ value: 0 }"

test "ServiceA External Egress" "http://$HOST:$SA_PORT/externalvalue" "0747532699"
test "ServiceA to ServiceB" "http://$HOST:$SA_PORT/servicebvalue-internal" "{ value: 0 }"

test "ServiceB External Egress (direct)" "http://$HOST:$SB_PORT/externalvalue" "0747532699"
test "ServiceB External Egress (through ServiceA)" "http://$HOST:$SA_PORT/servicebvalue-external" "0747532699"




