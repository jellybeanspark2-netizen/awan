import 'package:flutter/material.dart';

void main() {
  runApp(const AwanAaramApp());
}

class AwanAaramApp extends StatelessWidget {
  const AwanAaramApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Awan Aaram',
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.teal),
        useMaterial3: true,
      ),
      home: const Scaffold(
        body: Center(
          child: Text('Welcome to Awan Aaram - Smart Domestic Utilities'),
        ),
      ),
    );
  }
}
