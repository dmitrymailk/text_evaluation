#!/bin/bash

# use cuda 9.0 for bert-as-service
LD_LIBRARY_PATH=/usr/local/cuda-9.0/lib64/:$LD_LIBRARY_PATH


bert-serving-start -model_dir bert_model/uncased_L-12_H-768_A-12 \
    -num_worker 1 \
    -device_map 1 \
    -max_seq_len 512 \
    -pooling_strategy REDUCE_MEAN \
    -max_batch_size 1024 \
    -gpu_memory_fraction 1.0

