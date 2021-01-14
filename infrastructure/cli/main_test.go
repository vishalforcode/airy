package main

import (
	"fmt"
	"os/exec"
	"testing"

	airytests "cli/pkg/tests"
	"reflect"
)

const binaryName = "./airy"

func TestCli(t *testing.T) {
	tests := []struct {
		name    string
		args    []string
		golden  string
		wantErr bool
	}{
		{"no args", []string{}, "cli.no-args.golden", false},
		{"auth", []string{"auth", "--config", "pkg/tests/golden/airycli.yaml"}, "cli.auth.golden", false},
		{"auth", []string{"auth", "--config", "pkg/tests/golden/airycli.yaml", "--email", "grace@example.com"}, "cli.auth.golden", false},
		{"auth", []string{"auth", "--config", "pkg/tests/golden/airycli.yaml", "--email", "grace@example.com", "--password", "examplepassword"}, "cli.auth.golden", false},
		{"version", []string{"version", "--config", "pkg/tests/golden/airycli.yaml"}, "cli.version.golden", false},
	}

	go func() {
		airytests.MockServer()
	}()

	for _, tt := range tests {
		t.Run(tt.name, func(testing *testing.T) {
			cmd := exec.Command(binaryName, tt.args...)
			output, err := cmd.CombinedOutput()

			if (err != nil) != tt.wantErr {
				t.Fatalf("Test expected to fail: %t. Did the test pass: %t. Error message: %v\n", tt.wantErr, err == nil, err)
			}
			fmt.Println(output)

			actual := string(output)
			golden := airytests.NewGoldenFile(t, tt.golden)
			expected := golden.Load()

			if !reflect.DeepEqual(actual, expected) {
				t.Fatalf("diff: %v", airytests.Diff(actual, expected))
			}

		})

	}
}
