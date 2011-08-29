/**
 * Copyright 2010 Liang-Chi Hsieh
 * 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0 
 *        
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */
package org.unigram.likelike.lsh;

import java.io.IOException;
import java.io.*;
import java.util.*;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Mapper.Context;

import org.unigram.likelike.common.LikelikeConstants;
import org.unigram.likelike.common.RelatedUsersWritable;
import org.unigram.likelike.common.SeedClusterId;

/**
 * ReassignClusterIDsReducer. 
 */
public class ReassignClusterIDsReducer extends Reducer<Text, Text, Text, Text> {

    public static StringTokenizer tokenize(String line, String pattern) {
        StringTokenizer tokenizer = new StringTokenizer(line, pattern);
        return tokenizer;
    }

    public static StringTokenizer tokenize(Text value, String pattern) {
        String line = value.toString();
        StringTokenizer tokenizer = new StringTokenizer(line, pattern);
        return tokenizer;
    }

    
    /**
     * Reduce.
     *
     * @param key dummy
     * @param values cluster ids
     * @param context -
     * @throws IOException -
     * @throws InterruptedException -
     */
    @Override
    public void reduce(final Text key,
            final Iterable<Text> values,
            final Context context)
            throws IOException, InterruptedException {

        String reassigned_cluster_id = null;
        StringBuffer images = new StringBuffer("");
        int at_least_two_sampled_data = 0;

        for (Text value: values) {

            StringTokenizer images_tokenizer = tokenize(value, ":");

            if (images_tokenizer.countTokens() == 2) {
                // count

                images_tokenizer.nextToken();
                reassigned_cluster_id = images_tokenizer.nextToken();

            } else if (images_tokenizer.countTokens() == 1) {
                // images

                StringTokenizer data_sampled_type_tokenizer = tokenize(value, " %");
                while (data_sampled_type_tokenizer.hasMoreTokens()) {
                    String image_id = data_sampled_type_tokenizer.nextToken();
                    String data_sampled_type = data_sampled_type_tokenizer.nextToken();

                    if (at_least_two_sampled_data < 2 && data_sampled_type.equals("in"))
                        at_least_two_sampled_data++;

                    images.append(image_id + "%" + data_sampled_type + " ");
                }

            }
        }

        if (reassigned_cluster_id != null && !images.toString().equals("") && at_least_two_sampled_data >= 2)
           context.write(new Text(reassigned_cluster_id), new Text(images.toString()));

    }
   
    /**
     * setup.
     * @param context -
     */
    @Override
    public final void setup(final Context context) {

        Configuration jc = null;

        if (context != null) {
            jc = context.getConfiguration();
        }

        if (jc == null) {
            jc = new Configuration();
        }
    }
   
}

